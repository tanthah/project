/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controller;



import Model.Account;
import Model.Course;
import Model.Order;
import Model.Payment;
import Model.Student;
import dao.AccountDAO;
import dao.OrderDAO;
import dao.PaymentDAO;
import dao.CourseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/student/mycourses")
public class MyCoursesServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MyCoursesServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInAccount") == null) {
           
            response.sendRedirect(request.getContextPath() + "/views/LoginForStudent.jsp");
            return;
        }

        Account account = (Account) session.getAttribute("loggedInAccount");
        Long accountId = account.getAccountId();
        logger.info("Processing my courses for account ID: " + accountId);

        try {
            AccountDAO accountDAO = new AccountDAO();
            OrderDAO orderDAO = new OrderDAO();
            PaymentDAO paymentDAO = new PaymentDAO();
            CourseDAO courseDAO = new CourseDAO();

            // Lấy Student từ accountId
            Student student = accountDAO.getStudentByAccountId(accountId);
            if (student == null) {
               
                request.setAttribute("error", "Invalid student account.");
                request.getRequestDispatcher("/views/error.jsp").forward(request, response);
                return;
            }

            // Lấy danh sách Order của Student
            List<Order> orders = orderDAO.findByStudentId(accountId);
            List<Course> purchasedCourses = courseDAO.findPaidCourse(orders);
          
//            // Lọc các Order có Payment với status = PAID
//            for (Order order : orders) {
//                Payment payment = paymentDAO.findByOrderId(order.getOrderId());
//                if (payment != null && payment.getStatus() != null && payment.getStatus().name().equals("PAID")) {
//                    Course course = order.getCourse();
//                    if (course != null) {
//                        // Lấy thông tin đầy đủ của Course
//                        Course fullCourse = courseDAO.findById(course.getCourseId());
//                        if (fullCourse != null) {
//                            purchasedCourses.add(fullCourse);
//                        }
//                    }
//                }
//            }

           
            request.setAttribute("purchasedCourses", purchasedCourses);
            request.getRequestDispatcher("/views/myCourses.jsp").forward(request, response);

        } catch (Exception e) {
          
            request.setAttribute("error", "An error occurred while loading your courses: " + e.getMessage());
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
        }
    }
}