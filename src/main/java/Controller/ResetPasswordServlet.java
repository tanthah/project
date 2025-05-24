package Controller;

import dao.AccountDAO;
import dao.PasswordResetTokenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Model.Account;
import Model.PasswordResetToken;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private AccountDAO accountDAO;
    private PasswordResetTokenDAO tokenDAO;

    @Override
    public void init() throws ServletException {
        accountDAO = new AccountDAO();
        tokenDAO = new PasswordResetTokenDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token == null || token.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Liên kết không hợp lệ.");
            request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
            return;
        }

        PasswordResetToken prt = tokenDAO.findToken(token);
        if (prt == null || prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            request.setAttribute("errorMessage", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
            return;
        }

        request.setAttribute("tokenValid", true);
        request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String token = request.getParameter("token");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");

            // Kiểm tra token
            PasswordResetToken prt = tokenDAO.findToken(token);
            if (prt == null || prt.getExpiryDate().isBefore(LocalDateTime.now())) {
                request.setAttribute("errorMessage", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
                request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
                return;
            }

            // Kiểm tra mật khẩu
            if (password == null || password.trim().length() < 6) {
                request.setAttribute("errorMessage", "Mật khẩu phải có ít nhất 6 ký tự.");
                request.setAttribute("tokenValid", true);
                request.getRequestDispatcher("/WEB-INF/views/ResetPassword.jsp").forward(request, response);
                return;
            }

            if (!password.equals(confirmPassword)) {
                request.setAttribute("errorMessage", "Xác nhận mật khẩu không khớp.");
                request.setAttribute("tokenValid", true);
                request.getRequestDispatcher("/WEB-INF/views/ResetPassword.jsp").forward(request, response);
                return;
            }

            // Cập nhật mật khẩu
            Account account = accountDAO.findByUsernameOrEmail(null, prt.getEmail());
            if (account != null) {
                account.setPassword(password); // Lưu plaintext (nên băm bằng BCrypt trong tương lai)
                boolean updated = accountDAO.updateAccount(account);
                if (!updated) {
                    request.setAttribute("errorMessage", "Cập nhật mật khẩu thất bại. Vui lòng thử lại.");
                    request.setAttribute("tokenValid", true);
                    request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
                    return;
                }
            } else {
                request.setAttribute("errorMessage", "Tài khoản không tồn tại.");
                request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
                return;
            }

            // Xóa token
            tokenDAO.deleteToken(token);

            request.setAttribute("successMessage", "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập.");
            request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/ResetPassword.jsp").forward(request, response);
        }
    }
}