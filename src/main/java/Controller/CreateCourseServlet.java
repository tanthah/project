package servlet;

import dao.CategoryDAO;
import dao.CourseDAO;
import dao.AccountDAO;
import Model.Account;
import Model.Category;
import Model.Course;
import Model.Description;
import Model.Teacher;
import ENum.ScheduleDay;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/teacher/create-course")
public class CreateCourseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Kiểm tra xem account có phải là Teacher không
        Account account = (Account) session.getAttribute("account");
        AccountDAO accountDAO = new AccountDAO();
        if (!accountDAO.isTeacher(account.getAccountId())) {
            request.setAttribute("error", "You are not authorized to create courses.");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
            return;
        }

        // Lấy danh sách Category để hiển thị trong form
        CategoryDAO categoryDAO = CategoryDAO.getInstance();
        List<Category> categories = categoryDAO.findAll();
        request.setAttribute("categories", categories);

        // Chuyển tiếp đến form tạo khóa học
        request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Kiểm tra xem account có phải là Teacher không
        Account account = (Account) session.getAttribute("account");
        AccountDAO accountDAO = new AccountDAO();
        if (!accountDAO.isTeacher(account.getAccountId())) {
            request.setAttribute("error", "You are not authorized to create courses.");
            request.getRequestDispatcher("/views/teacher/managecourses.jsp").forward(request, response);
            return;
        }

        // Lấy dữ liệu từ form
        String title = request.getParameter("title");
        String priceStr = request.getParameter("price");
        String thumbnail = request.getParameter("thumbnail");
        String categoryIdStr = request.getParameter("categoryId");
        String descriptionContent = request.getParameter("descriptionContent");
        String[] applicableDaysArray = request.getParameterValues("applicableDays");

        // Validate dữ liệu
        if (title == null || title.trim().isEmpty() || priceStr == null || categoryIdStr == null || descriptionContent == null) {
            request.setAttribute("error", "All fields are required.");
            CategoryDAO categoryDAO = CategoryDAO.getInstance();
            request.setAttribute("categories", categoryDAO.findAll());
            request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
            return;
        }

        // Tạo đối tượng Course
        Course course = new Course();
        course.setTitle(title);
        try {
            course.setPrice(new BigDecimal(priceStr));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid price format.");
            CategoryDAO categoryDAO = CategoryDAO.getInstance();
            request.setAttribute("categories", categoryDAO.findAll());
            request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
            return;
        }
        course.setThumbnail(thumbnail);

        // Gán Category
        CategoryDAO categoryDAO = CategoryDAO.getInstance();
        Category category = categoryDAO.findById(Long.parseLong(categoryIdStr));
        if (category == null) {
            request.setAttribute("error", "Invalid category selected.");
            request.setAttribute("categories", categoryDAO.findAll());
            request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
            return;
        }
        course.setCategory(category);

        // Lấy Teacher từ Account
        Teacher teacher = accountDAO.getTeacherByAccountId(account.getAccountId());
        if (teacher == null) {
            request.setAttribute("error", "Teacher not found for this account.");
            request.setAttribute("categories", categoryDAO.findAll());
            request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
            return;
        }
        course.setTeacher(teacher);

        // Tạo và gán Description
        Description description = new Description();
        description.setContent(descriptionContent);

        // Xử lý applicableDays
        Set<ScheduleDay> applicableDays = new HashSet<>();
        if (applicableDaysArray != null) {
            for (String day : applicableDaysArray) {
                try {
                    applicableDays.add(ScheduleDay.valueOf(day));
                } catch (IllegalArgumentException e) {
                    request.setAttribute("error", "Invalid schedule day: " + day);
                    request.setAttribute("categories", categoryDAO.findAll());
                    request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
                    return;
                }
            }
        }
        description.getApplicableDays().addAll(applicableDays);

        course.setDescription(description);

        // Lưu Course vào DB
        CourseDAO courseDAO = new CourseDAO();
        boolean saved = courseDAO.saveCourse(course);

        if (saved) {
            request.setAttribute("message", "Course created successfully!");
            response.sendRedirect(request.getContextPath() + "/teacher/manage-courses");
        } else {
            request.setAttribute("error", "Failed to create course.");
            request.setAttribute("categories", categoryDAO.findAll());
            request.getRequestDispatcher("/views/teacher/createcourse.jsp").forward(request, response);
        }
    }
}