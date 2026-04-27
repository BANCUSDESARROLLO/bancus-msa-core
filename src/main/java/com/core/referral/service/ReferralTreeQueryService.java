package com.core.referral.service;

import com.core.integration.auth.AuthServiceClient;
import com.core.referral.dto.response.ReferralTreeDiagramNodeResponse;
import com.core.referral.dto.response.ReferralTreeDiagramResponse;
import com.core.referral.dto.response.ReferralTreeTableResponse;
import com.core.referral.dto.response.ReferralTreeTableRowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralTreeQueryService {

    private static final Comparator<TreeRow> BY_ID_REFERIDO =
            Comparator.comparing(TreeRow::idReferido, Comparator.nullsLast(Long::compareTo));

    private static final String USER_SUBTREE_QUERY_TEMPLATE = """
            SELECT t.ID_REFERIDO,
                   t.ID_REFERIDOR,
                   t.NIVEL,
                   t.FECHA_REGISTRO,
                   child.CODIGO_REFERIDO AS CODIGO_REFERIDO,
                   parent.CODIGO_REFERIDO AS CODIGO_REFERIDOR
            FROM (
                SELECT ? AS ID_REFERIDO,
                       CAST(NULL AS NUMBER) AS ID_REFERIDOR,
                       0 AS NIVEL,
                       CAST(NULL AS TIMESTAMP) AS FECHA_REGISTRO
                FROM dual
                UNION ALL
                SELECT rr.ID_REFERIDO,
                       rr.ID_REFERIDOR,
                       LEVEL AS NIVEL,
                       rr.FECHA_REGISTRO
                FROM %s.RED_REFERIDOS rr
                START WITH rr.ID_REFERIDOR = ?
                CONNECT BY PRIOR rr.ID_REFERIDO = rr.ID_REFERIDOR
                AND LEVEL <= 3
            ) t
            LEFT JOIN %s.USUARIO_REFERIDO_MAP child ON child.ID_REFERIDO = t.ID_REFERIDO
            LEFT JOIN %s.USUARIO_REFERIDO_MAP parent ON parent.ID_REFERIDO = t.ID_REFERIDOR
            ORDER BY t.NIVEL, t.ID_REFERIDO
            """;

    private static final String EXISTS_IN_NETWORK_QUERY_TEMPLATE = """
            SELECT COUNT(1)
            FROM %s.RED_REFERIDOS
            WHERE ID_REFERIDO = ? OR ID_REFERIDOR = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final AuthServiceClient authServiceClient;

    @Value("${core.referral.schema:APP_COREFINAN}")
    private String referralSchema;

    public ReferralTreeTableResponse getTreeAsTable(Long currentUserId) {
        validateUserId(currentUserId);

        List<TreeRow> treeRows = fetchCurrentUserSubtree(currentUserId);
        if (treeRows.isEmpty()) {
            return new ReferralTreeTableResponse(currentUserId, false, 0, List.of());
        }

        TreeContext context = buildContext(treeRows);
        TreeRow root = context.byId().get(currentUserId);
        if (root == null) {
            return new ReferralTreeTableResponse(currentUserId, false, 0, List.of());
        }

        List<ReferralTreeTableRowResponse> rows = new ArrayList<>();
        appendTableRows(root, new ArrayList<>(), new ArrayList<>(), context, rows);

        return new ReferralTreeTableResponse(
                currentUserId,
                existsInReferralNetwork(currentUserId),
                rows.size(),
                rows
        );
    }

    public ReferralTreeDiagramResponse getTreeAsDiagram(Long currentUserId) {
        validateUserId(currentUserId);

        List<TreeRow> treeRows = fetchCurrentUserSubtree(currentUserId);
        if (treeRows.isEmpty()) {
            return new ReferralTreeDiagramResponse(currentUserId, false, 0, List.of());
        }

        TreeContext context = buildContext(treeRows);
        TreeRow root = context.byId().get(currentUserId);
        if (root == null) {
            return new ReferralTreeDiagramResponse(currentUserId, false, 0, List.of());
        }

        Map<Long, String> usernameByUserId = new HashMap<>();
        Map<String, String> usernameByCode = new HashMap<>();
        ReferralTreeDiagramNodeResponse rootNode = buildDiagramNode(root, context, usernameByUserId, usernameByCode);
        if (rootNode == null) {
            return new ReferralTreeDiagramResponse(currentUserId, false, 0, List.of());
        }

        return new ReferralTreeDiagramResponse(
                currentUserId,
                existsInReferralNetwork(currentUserId),
                Math.max(context.byId().size() - 1, 0),
                List.of(rootNode)
        );
    }

    private void validateUserId(Long currentUserId) {
        if (currentUserId == null || currentUserId <= 0) {
            throw new IllegalArgumentException("currentUserId es obligatorio");
        }
    }

    private List<TreeRow> fetchCurrentUserSubtree(Long currentUserId) {
        String sql = USER_SUBTREE_QUERY_TEMPLATE.formatted(referralSchema, referralSchema, referralSchema);
        return jdbcTemplate.query(sql, this::mapTreeRow, currentUserId, currentUserId);
    }

    private boolean existsInReferralNetwork(Long currentUserId) {
        String sql = EXISTS_IN_NETWORK_QUERY_TEMPLATE.formatted(referralSchema);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, currentUserId, currentUserId);
        return count != null && count > 0;
    }

    private TreeRow mapTreeRow(ResultSet rs, int rowNum) throws SQLException {
        Long idReferido = rs.getLong("ID_REFERIDO");
        if (rs.wasNull()) {
            idReferido = null;
        }

        Long idReferidor = rs.getLong("ID_REFERIDOR");
        if (rs.wasNull()) {
            idReferidor = null;
        }

        Integer nivel = rs.getInt("NIVEL");
        if (rs.wasNull()) {
            nivel = null;
        }

        LocalDateTime fechaRegistro = null;
        if (rs.getTimestamp("FECHA_REGISTRO") != null) {
            fechaRegistro = rs.getTimestamp("FECHA_REGISTRO").toLocalDateTime();
        }

        return new TreeRow(
                idReferido,
                idReferidor,
                rs.getString("CODIGO_REFERIDO"),
                rs.getString("CODIGO_REFERIDOR"),
                nivel,
                fechaRegistro
        );
    }

    private TreeContext buildContext(List<TreeRow> rows) {
        Map<Long, TreeRow> byId = new LinkedHashMap<>();
        Map<Long, List<TreeRow>> childrenByParent = new HashMap<>();

        for (TreeRow row : rows) {
            byId.put(row.idReferido(), row);
            if (row.idReferidor() != null) {
                childrenByParent.computeIfAbsent(row.idReferidor(), ignored -> new ArrayList<>()).add(row);
            }
        }

        childrenByParent.values().forEach(list -> list.sort(BY_ID_REFERIDO));
        return new TreeContext(byId, childrenByParent);
    }

    private void appendTableRows(
            TreeRow current,
            List<String> parentPathIds,
            List<String> parentPathCodes,
            TreeContext context,
            List<ReferralTreeTableRowResponse> output
    ) {
        List<String> currentPathIds = new ArrayList<>(parentPathIds);
        currentPathIds.add(String.valueOf(current.idReferido()));

        List<String> currentPathCodes = new ArrayList<>(parentPathCodes);
        currentPathCodes.add(displayCode(current.codigoReferido(), current.idReferido()));

        output.add(new ReferralTreeTableRowResponse(
                current.idReferido(),
                current.idReferidor(),
                current.codigoReferido(),
                current.codigoReferidor(),
                current.nivel(),
                String.join(" > ", currentPathIds),
                String.join(" > ", currentPathCodes),
                current.fechaRegistro()
        ));

        for (TreeRow child : context.childrenByParent().getOrDefault(current.idReferido(), List.of())) {
            appendTableRows(child, currentPathIds, currentPathCodes, context, output);
        }
    }

    private ReferralTreeDiagramNodeResponse buildDiagramNode(
            TreeRow current,
            TreeContext context,
            Map<Long, String> usernameByUserId,
            Map<String, String> usernameByCode
    ) {
        String username = resolveUsername(current.idReferido(), current.codigoReferido(), usernameByUserId, usernameByCode);
        String usernameReferidor = resolveUsername(current.idReferidor(), current.codigoReferidor(), usernameByUserId, usernameByCode);
        List<ReferralTreeDiagramNodeResponse> referidos = new ArrayList<>();
        for (TreeRow child : context.childrenByParent().getOrDefault(current.idReferido(), List.of())) {
            ReferralTreeDiagramNodeResponse childNode = buildDiagramNode(child, context, usernameByUserId, usernameByCode);
            if (childNode != null) {
                referidos.add(childNode);
            }
        }

        return new ReferralTreeDiagramNodeResponse(
                current.idReferido(),
                current.idReferidor(),
                current.codigoReferido(),
                current.codigoReferidor(),
                username,
                usernameReferidor,
                current.fechaRegistro(),
                current.nivel(),
                referidos
        );
    }

    private String resolveUsername(
            Long idUsuario,
            String codigoReferido,
            Map<Long, String> usernameByUserId,
            Map<String, String> usernameByCode
    ) {
        String username = resolveUsernameByUserId(idUsuario, usernameByUserId);
        if (username != null) {
            return username;
        }
        return resolveUsernameByCode(codigoReferido, usernameByCode);
    }

    private String resolveUsernameByUserId(Long idUsuario, Map<Long, String> usernameByUserId) {
        if (idUsuario == null || idUsuario <= 0) {
            return null;
        }

        if (usernameByUserId.containsKey(idUsuario)) {
            return usernameByUserId.get(idUsuario);
        }

        try {
            String username = authServiceClient.getOwnerByUserId(idUsuario).nombreUsuario();
            usernameByUserId.put(idUsuario, username);
            return username;
        } catch (Exception ex) {
            log.warn("No se pudo resolver username en AUTH por idUsuario={}: {}", idUsuario, ex.getMessage());
            usernameByUserId.put(idUsuario, null);
            return null;
        }
    }

    private String resolveUsernameByCode(String codigoReferido, Map<String, String> usernameByCode) {
        if (codigoReferido == null || codigoReferido.isBlank()) {
            return null;
        }

        String normalizedCode = codigoReferido.trim().toUpperCase();
        if (usernameByCode.containsKey(normalizedCode)) {
            return usernameByCode.get(normalizedCode);
        }

        try {
            String username = authServiceClient.getOwnerByReferralCode(normalizedCode).nombreUsuario();
            usernameByCode.put(normalizedCode, username);
            return username;
        } catch (Exception ex) {
            log.warn("No se pudo resolver username en AUTH por codigoReferido={}: {}", normalizedCode, ex.getMessage());
            usernameByCode.put(normalizedCode, null);
            return null;
        }
    }

    private String displayCode(String code, Long id) {
        if (code != null && !code.isBlank()) {
            return code;
        }
        return id == null ? "" : String.valueOf(id);
    }

    private record TreeRow(
            Long idReferido,
            Long idReferidor,
            String codigoReferido,
            String codigoReferidor,
            Integer nivel,
            LocalDateTime fechaRegistro
    ) {
    }

    private record TreeContext(
            Map<Long, TreeRow> byId,
            Map<Long, List<TreeRow>> childrenByParent
    ) {
    }
}
