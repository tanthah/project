package Controller;

import dao.AccountDAO;
import dao.PasswordResetTokenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Model.Account;
import Util.EmailUtil;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private AccountDAO accountDAO;
    private PasswordResetTokenDAO tokenDAO;

    @Override
    public void init() throws ServletException {
        accountDAO = new AccountDAO();
        tokenDAO = new PasswordResetTokenDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/ForgotPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String email = request.getParameter("email");

            // Kiểm tra email
            if (email == null || email.trim().isEmpty()) {
                request.setAttribute("errorMessage", "Vui lòng nhập email.");
                request.getRequestDispatcher("/WEB-INF/views/ForgotPassword.jsp").forward(request, response);
                return;
            }

            // Kiểm tra email tồn tại
            Account account = accountDAO.findByUsernameOrEmail(null, email);
            if (account == null) {
                request.setAttribute("errorMessage", "Email không tồn tại.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/WEB-INF/views/ForgotPassword.jsp").forward(request, response);
                return;
            }

            // Tạo token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
            tokenDAO.saveToken(email, token, expiryDate);

            // Gửi email
            String resetLink = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                              request.getContextPath() + "/reset-password?token=" + token;
            EmailUtil.sendEmail(email, "Đặt Lại Mật Khẩu",
                                "Nhấp vào liên kết sau để đặt lại mật khẩu của bạn:\n" + resetLink +
                                "\nLiên kết này có hiệu lực trong 1 giờ.");

            request.setAttribute("successMessage", "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
            request.getRequestDispatcher("/views/ForgotPassword.jsp").forward(request, response);
        } catch (MessagingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi khi gửi email. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/ForgotPassword.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/ForgotPassword.jsp").forward(request, response);
        }
    }
}