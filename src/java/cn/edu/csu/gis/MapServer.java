/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.csu.gis;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pcwang
 */
@WebServlet(name = "MapServer", urlPatterns = {"/MapServer"})
public class MapServer extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            int width =Integer.valueOf(request.getParameter("width"));
            int height =Integer.valueOf(request.getParameter("height"));
            double minx =Double.valueOf(request.getParameter("minx"));
            double miny =Double.valueOf(request.getParameter("miny"));
            double maxx =Double.valueOf(request.getParameter("maxx"));
            double maxy =Double.valueOf(request.getParameter("maxy"));
            String format =request.getParameter("format");
//            String[] layers = request.getParameter("layers").toString().split(",");
            int srid = Integer.valueOf(request.getParameter("srid"));
            
            MapRender render= new MapRender(width, height, format);
            render.setSrid(srid);
            render.setExtent(minx, miny, maxx, maxy);
//            render.setLayers(layers);
            render.render();
            render.saveTo(out);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
