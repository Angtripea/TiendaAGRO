package com.tienda.servlets;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class GuardarVentaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Credenciales de tu base de datos
    private final String DB_URL = "jdbc:mysql://localhost:3306/prueba";
    private final String USER = "root";
    private final String PASS = "M$r13l3n4";

    // Clases internas para mapear el JSON
    static class Producto {
        String title;
        String price;
        int quantity;
    }

    static class Venta {
        String email;
        String password;
        String tarjeta;
        int total;
        List<Producto> productos;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (BufferedReader reader = request.getReader()) {

            // Leer JSON del body con GSON
            Gson gson = new Gson();
            Venta venta = gson.fromJson(reader, Venta.class);

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {

                conn.setAutoCommit(false);

                // Insertar en tabla compras
                String sqlCompra = "INSERT INTO compras (email, password, tarjeta, total) VALUES (?, ?, ?, ?)";
                PreparedStatement stmtCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
                stmtCompra.setString(1, venta.email);
                stmtCompra.setString(2, venta.password);
                stmtCompra.setString(3, venta.tarjeta);
                stmtCompra.setInt(4, venta.total);
                stmtCompra.executeUpdate();

                ResultSet rs = stmtCompra.getGeneratedKeys();
                int idCompra = -1;
                if (rs.next()) {
                    idCompra = rs.getInt(1);
                }

                // Insertar productos
                String sqlProd = "INSERT INTO productos_compra (id_compra, titulo, precio, cantidad) VALUES (?, ?, ?, ?)";
                PreparedStatement stmtProd = conn.prepareStatement(sqlProd);

                for (Producto p : venta.productos) {
                    int precio = Integer.parseInt(p.price.replace("$", ""));
                    stmtProd.setInt(1, idCompra);
                    stmtProd.setString(2, p.title);
                    stmtProd.setInt(3, precio);
                    stmtProd.setInt(4, p.quantity);
                    stmtProd.addBatch();
                }

                stmtProd.executeBatch();
                conn.commit();

                Map<String, Object> successResp = new HashMap<>();
                successResp.put("success", true);
                successResp.put("id_compra", idCompra);

                out.print(gson.toJson(successResp));

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResp = new HashMap<>();
                errorResp.put("success", false);
                errorResp.put("message", "Error al guardar en la base de datos: " + e.getMessage());
                out.print(gson.toJson(errorResp));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Error procesando la solicitud\"}");
        }
    }
}