package Controller;
import org.mindrot.jbcrypt.BCrypt;

import dao.AccountDAO; // Import lớp DAO của bạn
import Model.Account; // Import lớp Model của bạn

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login") // Map Servlet tới URL /login
public class LoginServlet extends HttpServlet {

      private AccountDAO accountDAO;

    @Override
    public void init() throws ServletException {
        // Khởi tạo AccountDAO
        accountDAO = new AccountDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Hiển thị trang đăng nhập (ví dụ: login.jsp)
        request.getRequestDispatcher("views/home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password"); // Cần băm mật khẩu để so sánh

        // **Lưu ý:** Thực hiện xác thực đầu vào ở đây (kiểm tra null, rỗng, định dạng...)

        Account account = accountDAO.findByUsernameAndPassword(username, password); // Cần truyền mật khẩu đã băm

        if (account != null) {
            // Đăng nhập thành công
            HttpSession session = request.getSession(true); // Tạo session mới nếu chưa có
            session.setAttribute("loggedInAccount", account); // Lưu thông tin người dùng vào session

            // Chuyển hướng đến trang chào mừng hoặc trang chính
            response.sendRedirect(request.getContextPath() + "/views/StudentDashboard.jsp"); // Ví dụ: chuyển hướng đến /home
        } else {
            // Đăng nhập thất bại
            request.setAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác.");
            // Quay lại trang đăng nhập và hiển thị lỗi
            request.getRequestDispatcher("/views/Login.jsp").forward(request, response); //"/views/Login.jsp"
        }
    }

    @Override
    public void destroy() {
        // Đóng tài nguyên nếu cần
        super.destroy();
    }
}