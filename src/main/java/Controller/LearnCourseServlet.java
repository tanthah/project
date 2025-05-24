package Controller;

import Model.Chapter;
import Model.Course;
import Model.Lesson;
import dao.ChapterDAO;
import dao.CourseDAO;
import dao.LessonDAO;
import dao.FileMediaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/student/learn-course")
public class LearnCourseServlet extends HttpServlet {

    private CourseDAO courseDAO = new CourseDAO();
    private ChapterDAO chapterDAO = new ChapterDAO();
    private LessonDAO lessonDAO = new LessonDAO();
    private FileMediaDAO fileMediaDAO = new FileMediaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long courseId = Long.parseLong(request.getParameter("courseId"));
        
        Course course = courseDAO.findById(courseId);
        
        if (course != null) {
            List<Chapter> chapters = chapterDAO.findByCourseId(courseId);
            for (Chapter chapter : chapters) {
                List<Lesson> lessons = lessonDAO.findByChapterId(chapter.getChapterId());
                for (Lesson lesson : lessons) {
                    lesson.setFileMedias(fileMediaDAO.findByLessonId(lesson.getLessonId()));
                }
                chapter.setLessons(lessons);
            }
            course.setChapters(chapters);
            
            request.setAttribute("course", course);
            request.getRequestDispatcher("/views/Learnpage.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Course not found.");
            response.sendRedirect("/student/courses");
        }
    }
}