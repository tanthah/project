/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controller;

import Model.Course;
import dao.CourseDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


@WebServlet(name = "BrowseCourseServlet", urlPatterns = {"/BrowseCourseServlet"})
public class BrowseCourseServlet extends HttpServlet {

    private CourseDAO courseDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        // Khởi tạo đối tượng CourseDAO
        courseDAO = new CourseDAO();
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy giá trị searchKeyword và sortOrder từ form
        String searchKeyword = request.getParameter("searchKeyword");
        String sortOrder = request.getParameter("sortOrder");

        // Mặc định sortOrder là "asc" nếu không có giá trị
        if (sortOrder == null) {
            sortOrder = "asc";  // Sử dụng mặc định nếu không có giá trị
        }

        // Tìm khóa học theo tiêu đề và sắp xếp theo giá
        List<Course> courses = courseDAO.findCoursesByTitleSortedByPrice(searchKeyword, sortOrder);

        // Gửi kết quả đến JSP để hiển thị
        request.setAttribute("courses", courses);
        request.setAttribute("showCourses", true);
        request.getRequestDispatcher("/views/courses.jsp").forward(request, response);
    }
    
   @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Khởi tạo DAO để lấy danh sách khóa học
        CourseDAO courseDAO = new CourseDAO();

        // Lấy tất cả khóa học
        List<Course> courses = courseDAO.findAll();

        // Nếu không có khóa học, tạo danh sách rỗng
        if (courses == null) {
            courses = new ArrayList<>();
        }

        // Đặt các thông tin vào request để chuyển đến JSP
        request.setAttribute("courses", courses);
        request.setAttribute("showCourses", true);  // Luôn hiển thị danh sách khóa học

        // Chuyển tiếp tới trang JSP
        request.getRequestDispatcher("/views/index.jsp").forward(request, response);
    }

}
