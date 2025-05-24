package Controller;

import dao.AccountDAO;
import dao.AdminDAO;
import Model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/stafflogin")
public class StaffLoginServlet extends HttpServlet {
    
    private AccountDAO accountDAO;
    private AdminDAO adminDAO;

    @Override
    public void init() throws ServletException {
        // Khởi tạo AccountDAO và AdminDAO
        accountDAO = new AccountDAO();
        adminDAO = new AdminDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đảm bảo encoding của request và response
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Lấy thông tin từ form
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        

        // Kiểm tra xem các tham số có null không
        if (username == null || password == null ) {
            request.setAttribute("error", "Please provide username, password, and role.");
            request.getRequestDispatcher("/views/ERROR2.jsp").forward(request, response);
            return;
        }

        // Tìm tài khoản dựa trên username
        Account account = accountDAO.findByUsernameOrEmail(username, null);

        // Kiểm tra tài khoản và so sánh mật khẩu
        if (account != null && account.isActive() && password.equals(account.getPassword())) {
            // Lưu tài khoản vào session
            HttpSession session = request.getSession();
            session.setAttribute("account", account);

            // Điều hướng dựa trên vai trò
            Long accountId = account.getAccountId();
            try {
                if ( accountDAO.isTeacher(accountId)) {
                    response.sendRedirect(request.getContextPath() + "/views/teacher/teacherDashboard.jsp");
                } else if ( adminDAO.findAdminById(accountId)) {
                    response.sendRedirect(request.getContextPath() + "/views/admin/adminDashboard.jsp");
                } else {
                    // Vai trò không khớp hoặc tài khoản không thuộc loại tương ứng
                    request.setAttribute("error", "Invalid role or account type.");
                    request.getRequestDispatcher("/views/ERROR2.jsp").forward(request, response);
                }
            } catch (Exception e) {
                request.setAttribute("error", "Error checking role: " + e.getMessage());
                request.getRequestDispatcher("/views/ERROR2.jsp").forward(request, response);
            }
        } else {
            // Đăng nhập thất bại
            request.setAttribute("error", "Invalid username or password.");
            request.getRequestDispatcher("/views/ERROR2.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Nếu truy cập bằng GET, chuyển hướng đến trang đăng nhập
        response.sendRedirect(request.getContextPath() + "/views/LoginForStaff.jsp");
    }
}