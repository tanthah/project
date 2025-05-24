
package Controller;

import ENum.PaymentMethod;
import ENum.PaymentStatus;
import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Model.*;
import dao.CourseDAO;
import dao.OrderDAO;
import dao.PaymentDAO;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/buycourse")
public class BuyCourseServlet extends HttpServlet {

    private CourseDAO courseDAO = new CourseDAO();
    private StudentDAO studentDAO = new StudentDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("doGet: Bắt đầu xử lý yêu cầu mua khóa học");
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("loggedInAccount") != null) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInAccount");
        System.out.println("doGet: loggedInAccount ID = " + loggedInStudent.getAccountId());
        Student studentFromDB = studentDAO.findById(loggedInStudent.getAccountId());
        if (studentFromDB != null) {
            try {
                String courseIdStr = request.getParameter("courseId");
                System.out.println("doGet: courseId = " + courseIdStr);
                if (courseIdStr == null || courseIdStr.trim().isEmpty()) {
                    request.setAttribute("errorMessage", "ID khóa học bị thiếu");
                    request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
                    return;
                }

                Long courseId = Long.parseLong(courseIdStr);
                Course course = courseDAO.findById(courseId);
                System.out.println("doGet: Course found = " + (course != null));
                if (course != null) {
                    Order order = new Order(LocalDateTime.now(), course.getPrice(), studentFromDB, course);
                    orderDAO.saveOrder(order);
                    System.out.println("doGet: Order saved, orderId = " + order.getOrderId());

                    Payment payment = new Payment(
                        course.getPrice(), LocalDateTime.now(), PaymentMethod.MOMO, PaymentStatus.PENDING, order
                    );
                    paymentDAO.savePayment(payment);
                    System.out.println("doGet: Payment saved, paymentId = " + payment.getPaymentId());

                    request.setAttribute("order", order);
                    request.setAttribute("payment", payment);
                    request.setAttribute("course", course);

                    // Chuyển tiếp đến OrderConfirmation.jsp
                    request.getRequestDispatcher("/views/OrderConfirmation.jsp").forward(request, response);
                } else {
                    request.setAttribute("errorMessage", "Không tìm thấy khóa học với ID: " + courseId);
                    request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "Định dạng ID khóa học không hợp lệ: " + request.getParameter("courseId"));
                request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi không mong muốn khi xử lý: " + e.getMessage());
                request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("errorMessage", "Tài khoản sinh viên không hợp lệ");
            request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
        }
    } else {
        System.out.println("doGet: Không có session hoặc chưa đăng nhập");
        response.sendRedirect(request.getContextPath() + "/views/Login.jsp");
    }
}

    // Phương thức doPost để cập nhật Payment status từ PENDING thành PAID
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
        // Lấy paymentId từ request
        Long paymentId = Long.parseLong(request.getParameter("paymentId"));
        // Tìm Payment theo paymentId
        Payment payment = paymentDAO.findById(paymentId);

        if (payment != null) {
            // Kiểm tra trạng thái thanh toán hiện tại
            if (payment.getStatus() == PaymentStatus.PENDING) {
                // Cập nhật trạng thái thanh toán thành PAID
                payment.setStatus(PaymentStatus.PAID);
                paymentDAO.savePayment(payment); // Lưu lại Payment

                // Chuyển hướng đến trang xác nhận thành công
                response.sendRedirect(request.getContextPath() + "/views/index.jsp");
            } else {
                // Nếu trạng thái không phải PENDING, hiển thị lỗi
                request.setAttribute("errorMessage", "Payment status is already " + payment.getStatus() + ". Cannot update.");
                request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
            }
        } else {
            // Nếu không tìm thấy Payment
            request.setAttribute("errorMessage", "Payment not found for ID: " + paymentId);
            request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
        }
    } catch (NumberFormatException e) {
        // Nếu paymentId không hợp lệ
        request.setAttribute("errorMessage", "Invalid payment ID format: " + request.getParameter("paymentId"));
        request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
    } catch (Exception e) {
        // Xử lý các lỗi không mong muốn
        e.printStackTrace();
        request.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
    }
}

}
