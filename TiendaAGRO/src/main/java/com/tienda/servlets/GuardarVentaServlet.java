package com.tienda.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.*;

public class GuardarVentaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Cambia según tus credenciales MySQL
    private final String DB_URL = "jdbc:mysql://localhost:3306/prueba";
    private final String USER = "root";
    private final String PASS = "M$r13l3n4";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leer JSON del cuerpo
        BufferedReader reader = request.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            JSONObject json = new JSONObject(jsonBuilder.toString());

            String email = json.getString("email");
            String password = json.getString("password");
            String tarjeta = json.getString("tarjeta");
            int total = json.getInt("total");
            JSONArray productos = json.getJSONArray("productos");

            conn.setAutoCommit(false);

            // Insertar compra
            String sqlCompra = "INSERT INTO compras (email, password, tarjeta, total) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            stmtCompra.setString(1, email);
            stmtCompra.setString(2, password);
            stmtCompra.setString(3, tarjeta);
            stmtCompra.setInt(4, total);
            stmtCompra.executeUpdate();

            ResultSet rs = stmtCompra.getGeneratedKeys();
            int idCompra = -1;
            if (rs.next()) {
                idCompra = rs.getInt(1);
            }

            // Insertar productos
            String sqlProd = "INSERT INTO productos_compra (id_compra, titulo, precio, cantidad) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtProd = conn.prepareStatement(sqlProd);

            for (int i = 0; i < productos.length(); i++) {
                JSONObject p = productos.getJSONObject(i);
                String titulo = p.getString("title");
                int precio = Integer.parseInt(p.getString("price").replace("$", ""));
                int cantidad = p.getInt("quantity");

                stmtProd.setInt(1, idCompra);
                stmtProd.setString(2, titulo);
                stmtProd.setInt(3, precio);
                stmtProd.setInt(4, cantidad);
                stmtProd.addBatch();
            }

            stmtProd.executeBatch();
            conn.commit();

            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true,\"id_compra\":" + idCompra + "}");

        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
