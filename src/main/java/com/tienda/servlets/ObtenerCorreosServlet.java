package com.tienda.servlets;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class ObtenerCorreosServlet extends HttpServlet {
    private final String DB_URL = "jdbc:mysql://localhost:3306/prueba";
    private final String USER = "root";
    private final String PASS = "M$r13l3n4";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT DISTINCT email FROM compras"; // O la tabla donde tengas los emails
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                List<String> emails = new ArrayList<>();
                while (rs.next()) {
                    emails.add(rs.getString("email"));
                }

                Gson gson = new Gson();
                out.print(gson.toJson(emails));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
