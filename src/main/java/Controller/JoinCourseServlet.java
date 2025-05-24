package Controller;

import dao.CourseDAO;
import dao.OrderDAO;
import dao.AccountDAO;
import Model.Account;
import Model.Student;
import Model.Course;
import Model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@WebServlet("/student/join-course")
public class JoinCourseServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/views/Login.jsp");
            return;
        }

        // Lấy Account từ session
        Account account = (Account) session.getAttribute("account");
        AccountDAO accountDAO = new AccountDAO();

        // Kiểm tra xem account có phải là Student không
        Student student = accountDAO.getStudentByAccountId(account.getAccountId());
        if (student == null) {
            request.setAttribute("error", "You must be a student to join a course.");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
            return;
        }

        // Lấy courseId từ form
        String courseIdStr = request.getParameter("courseId");
        try {
            Long courseId = Long.parseLong(courseIdStr);
            CourseDAO courseDAO = new CourseDAO();
            Course course = courseDAO.findById(courseId);

            if (course == null) {
                request.setAttribute("error", "Course not found.");
                request.getRequestDispatcher("/views/error.jsp").forward(request, response);
                return;
            }

            // Tạo Order mới
            Order order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setTotalPrice(course.getPrice());
            order.setStudent(student);
            order.setCourse(course);

            // Lưu Order
            OrderDAO orderDAO = new OrderDAO();
            boolean saved = orderDAO.saveOrder(order);

            if (saved) {
                request.setAttribute("message", "Successfully joined the course: " + course.getTitle());
            } else {
                request.setAttribute("error", "Failed to join the course.");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid course ID.");
        }

        // Chuyển tiếp về trang quản lý khóa học
        request.getRequestDispatcher("/views/courses.jsp").forward(request, response);
    }
}