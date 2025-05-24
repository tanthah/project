package Controller;

import dao.AccountDAO;
import Model.Student;
import Model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private AccountDAO accountDAO;

    @Override
    public void init() throws ServletException {
        // Khởi tạo AccountDAO
        accountDAO = new AccountDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Hiển thị trang đăng ký
        request.getRequestDispatcher("/views/Register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy và trim dữ liệu từ form
        String username = request.getParameter("username") != null ? request.getParameter("username").trim() : null;
        String password = request.getParameter("password") != null ? request.getParameter("password").trim() : null;
        String email = request.getParameter("email") != null ? request.getParameter("email").trim() : null;
        String phone = request.getParameter("phone") != null ? request.getParameter("phone").trim() : "";

        // Map để lưu lỗi validation
        Map<String, String> errors = new HashMap<>();

        // Validation
        if (username == null || username.isEmpty()) {
            errors.put("username", "Tên đăng nhập là bắt buộc");
        } else if (username.length() < 3) {
            errors.put("username", "Tên đăng nhập phải có ít nhất 3 ký tự");
        }

        if (password == null || password.isEmpty()) {
            errors.put("password", "Mật khẩu là bắt buộc");
        } else if (password.length() < 6) {
            errors.put("password", "Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (email == null || email.isEmpty()) {
            errors.put("email", "Email là bắt buộc");
        } else if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            errors.put("email", "Email không đúng định dạng");
        }

        if (!phone.isEmpty() && !phone.matches("^\\d{10,11}$")) {
            errors.put("phone", "Số điện thoại phải có 10-11 chữ số");
        }

        // Nếu có lỗi validation, gửi lại form
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.setAttribute("errorMessage", "Vui lòng sửa các lỗi sau:");
            request.getRequestDispatcher("/views/Register.jsp").forward(request, response);
            return;
        }

        // Kiểm tra username hoặc email đã tồn tại
        Account existingAccount = accountDAO.findByUsernameOrEmail(username, email);
        if (existingAccount != null) {
            request.setAttribute("errorMessage", "Tên đăng nhập hoặc email đã tồn tại.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/views/Register.jsp").forward(request, response);
            return;
        }

        // Mã hóa mật khẩu
        String hashedPassword = hashPassword(password);

        // Tạo đối tượng Student
        Student newStudent = new Student(username, hashedPassword, email, phone, null, true);

        // Lưu tài khoản vào database
        boolean success = accountDAO.saveAccount(newStudent);

        if (success) {
            // Đăng ký thành công, chuyển hướng đến trang đăng nhập
            response.sendRedirect(request.getContextPath() + "/views/Login.jsp");
        } else {
            // Lưu thất bại
            request.setAttribute("errorMessage", "Đăng ký thất bại. Vui lòng thử lại sau.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/views/Register.jsp").forward(request, response);
        }
    }

    private String hashPassword(String password) {
        // Giả sử sử dụng BCrypt hoặc hàm mã hóa khác
        // Ví dụ: return BCrypt.hashpw(password, BCrypt.gensalt());
        return password; // Thay bằng hàm mã hóa thực tế
    }

    @Override
    public void destroy() {
        // Đóng tài nguyên nếu cần
        super.destroy();
    }
}