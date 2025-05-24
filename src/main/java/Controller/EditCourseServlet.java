package Controller;

import Model.Category;
import Model.Chapter;
import Model.Course;
import Model.Lesson;
import Model.FileMedia;
import dao.CategoryDAO;
import dao.ChapterDAO;
import dao.CourseDAO;
import dao.LessonDAO;
import dao.FileMediaDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@WebServlet("/teacher/edit-course")
public class EditCourseServlet extends HttpServlet {

    private CourseDAO courseDAO = new CourseDAO();
    private CategoryDAO categoryDAO = CategoryDAO.getInstance();
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
            
            List<Category> categories = categoryDAO.findAll();
            request.setAttribute("course", course);
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Course not found.");
            response.sendRedirect("/teacher/manage-courses");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("updateCourse".equals(action)) {
            Long courseId = Long.parseLong(request.getParameter("courseId"));
            String title = request.getParameter("title");
            String priceParam = request.getParameter("price");
            String thumbnail = request.getParameter("thumbnail");
            Long categoryId = Long.parseLong(request.getParameter("categoryId"));
            String descriptionContent = request.getParameter("descriptionContent");

            BigDecimal price = null;
            if (priceParam != null && !priceParam.isEmpty()) {
                price = new BigDecimal(priceParam);
            } else {
                price = BigDecimal.ZERO;
            }

            Course course = courseDAO.findById(courseId);
            if (course != null) {
                Category category = categoryDAO.findById(categoryId);
                if (category != null) {
                    course.setTitle(title);
                    course.setPrice(price);
                    course.setThumbnail(thumbnail);
                    course.setCategory(category);
                    course.getDescription().setContent(descriptionContent);

                    boolean success = courseDAO.saveCourse(course);
                    
                    List<Category> categories = categoryDAO.findAll();

                    if (success) {
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
                        request.setAttribute("categories", categories);
                        request.setAttribute("message", "Course updated successfully.");
                        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
                    } else {
                        request.setAttribute("error", "Failed to update course.");
                        request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
                    }
                } else {
                    request.setAttribute("error", "Category not found.");
                    request.getRequestDispatcher("/views/ErrorPage.jsp").forward(request, response);
                }
            } else {
                request.setAttribute("error", "Course not found.");
                response.sendRedirect("/teacher/manage-courses");
            }
        } else if ("addChapter".equals(action)) {
            addChapter(request, response);
        } else if ("updateChapter".equals(action)) {
            updateChapter(request, response);
        } else if ("delChapter".equals(action)) {
            deleteChapter(request, response);
        } else if ("addLesson".equals(action)) {
            addLesson(request, response);
        } else if ("updateLesson".equals(action)) {
            updateLesson(request, response);
        } else if ("deleteLesson".equals(action)) {
            deleteLesson(request, response);
        } else if ("addFileMedia".equals(action)) {
            addFileMedia(request, response);
        } else if ("updateFileMedia".equals(action)) {
            updateFileMedia(request, response);
        } else if ("deleteFileMedia".equals(action)) {
            deleteFileMedia(request, response);
        }
    }

    private void addChapter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long courseId = Long.parseLong(request.getParameter("courseId"));
        String title = request.getParameter("chapterTitle");
        Integer chapterOrder = Integer.parseInt(request.getParameter("chapterOrder"));

        Course course = courseDAO.findById(courseId);
        Chapter chapter = new Chapter();
        chapter.setTitle(title);
        chapter.setChapterOrder(chapterOrder);
        chapter.setCourse(course);

        boolean success = chapterDAO.saveChapter(chapter);

        if (success) {
            List<Chapter> chapters = chapterDAO.findByCourseId(courseId);
            for (Chapter ch : chapters) {
                List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                for (Lesson lesson : lessons) {
                    lesson.setFileMedias(fileMediaDAO.findByLessonId(lesson.getLessonId()));
                }
                ch.setLessons(lessons);
            }
            course.setChapters(chapters);
            request.setAttribute("message", "Chapter added successfully.");
        } else {
            request.setAttribute("error", "Failed to add chapter.");
        }

        request.setAttribute("course", course);
        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void updateChapter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));
        String title = request.getParameter("chapterTitle");
        Integer chapterOrder = Integer.parseInt(request.getParameter("chapterOrder"));

        Chapter chapter = chapterDAO.findById(chapterId);
        if (chapter != null) {
            chapter.setTitle(title);
            chapter.setChapterOrder(chapterOrder);
            boolean success = chapterDAO.saveChapter(chapter);

            if (success) {
                request.setAttribute("message", "Chapter updated successfully.");
            } else {
                request.setAttribute("error", "Failed to update chapter.");
            }
        } else {
            request.setAttribute("error", "Chapter not found.");
        }

        Course course = chapter.getCourse();
        List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
        for (Chapter ch : chapters) {
            List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
            for (Lesson lesson : lessons) {
                lesson.setFileMedias(fileMediaDAO.findByLessonId(lesson.getLessonId()));
            }
            ch.setLessons(lessons);
        }
        course.setChapters(chapters);

        request.setAttribute("course", course);
        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void deleteChapter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));

        boolean success = chapterDAO.deleteChapter(chapterId);
        if (success) {
            request.setAttribute("message", "Chapter deleted successfully.");
        } else {
            request.setAttribute("error", "Failed to delete chapter.");
        }

        Long courseId = Long.parseLong(request.getParameter("courseId"));
        Course course = courseDAO.findById(courseId);
        if (course != null) {
            List<Chapter> chapters = chapterDAO.findByCourseId(courseId);
            for (Chapter ch : chapters) {
                List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                for (Lesson lesson : lessons) {
                    lesson.setFileMedias(fileMediaDAO.findByLessonId(lesson.getLessonId()));
                }
                ch.setLessons(lessons);
            }
            course.setChapters(chapters);
        }

        request.setAttribute("course", course);
        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void addLesson(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));
        String lessonId = request.getParameter("lessonId");
        String title = request.getParameter("lessonTitle");
        Integer lessonIndex = Integer.parseInt(request.getParameter("lessonIndex"));
        String description = request.getParameter("lessonDescription");

        Chapter chapter = chapterDAO.findById(chapterId);

        if (chapter != null) {
            Lesson lesson = new Lesson(title, description, lessonIndex, chapter);
            lesson.setLessonId(lessonId != null && !lessonId.isEmpty() ? lessonId : UUID.randomUUID().toString());

            boolean success = lessonDAO.saveLesson(lesson);
            
            if (success) {
                chapter.setLessons(lessonDAO.findByChapterId(chapterId));
                Course course = chapter.getCourse();
                List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
                for (Chapter ch : chapters) {
                    List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                    for (Lesson l : lessons) {
                        l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                    }
                    ch.setLessons(lessons);
                }
                course.setChapters(chapters);
                request.setAttribute("course", course);
                request.setAttribute("message", "Lesson added successfully.");
            } else {
                request.setAttribute("error", "Failed to add lesson.");
                request.setAttribute("course", chapter.getCourse());
            }
        } else {
            request.setAttribute("error", "Chapter not found.");
            Course course = courseDAO.findById(Long.parseLong(request.getParameter("courseId")));
            request.setAttribute("course", course);
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void updateLesson(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lessonId = request.getParameter("lessonId");
        String title = request.getParameter("lessonTitle");
        Integer lessonIndex = Integer.parseInt(request.getParameter("lessonIndex"));
        String description = request.getParameter("lessonDescription");
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));

        Lesson lesson = lessonDAO.findById(lessonId);
        Chapter chapter = chapterDAO.findById(chapterId);
        if (lesson != null && chapter != null) {
            lesson.setTitle(title);
            lesson.setLessonIndex(lessonIndex);
            lesson.setDescription(description);
            lesson.setChapter(chapter);
            boolean success = lessonDAO.saveLesson(lesson);

            if (success) {
                chapter.setLessons(lessonDAO.findByChapterId(chapterId));
                Course course = chapter.getCourse();
                List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
                for (Chapter ch : chapters) {
                    List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                    for (Lesson l : lessons) {
                        l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                    }
                    ch.setLessons(lessons);
                }
                course.setChapters(chapters);
                request.setAttribute("course", course);
                request.setAttribute("message", "Lesson updated successfully.");
            } else {
                request.setAttribute("error", "Failed to update lesson.");
                request.setAttribute("course", chapter.getCourse());
            }
        } else {
            request.setAttribute("error", lesson == null ? "Lesson not found." : "Chapter not found.");
            Course course = courseDAO.findById(Long.parseLong(request.getParameter("courseId")));
            request.setAttribute("course", course);
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void deleteLesson(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lessonId = request.getParameter("lessonId");
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));

        boolean success = lessonDAO.deleteLesson(lessonId);
        if (success) {
            Chapter chapter = chapterDAO.findById(chapterId);
            chapter.setLessons(lessonDAO.findByChapterId(chapterId));
            Course course = chapter.getCourse();
            List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
            for (Chapter ch : chapters) {
                List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                for (Lesson l : lessons) {
                    l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                }
                ch.setLessons(lessons);
            }
            course.setChapters(chapters);
            request.setAttribute("course", course);
            request.setAttribute("message", "Lesson deleted successfully.");
        } else {
            request.setAttribute("error", "Failed to delete lesson.");
            Chapter chapter = chapterDAO.findById(chapterId);
            request.setAttribute("course", chapter.getCourse());
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void addFileMedia(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lessonId = request.getParameter("lessonId");
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));
        String fileId = request.getParameter("fileId");
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String fileUrl = request.getParameter("fileUrl");

        Lesson lesson = lessonDAO.findById(lessonId);
        Chapter chapter = chapterDAO.findById(chapterId);
        if (lesson != null && chapter != null) {
            FileMedia fileMedia = new FileMedia(fileId, fileName, fileType, fileUrl, lesson);
            boolean success = fileMediaDAO.saveFileMedia(fileMedia);

            if (success) {
                lesson.setFileMedias(fileMediaDAO.findByLessonId(lessonId));
                chapter.setLessons(lessonDAO.findByChapterId(chapterId));
                Course course = chapter.getCourse();
                List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
                for (Chapter ch : chapters) {
                    List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                    for (Lesson l : lessons) {
                        l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                    }
                    ch.setLessons(lessons);
                }
                course.setChapters(chapters);
                request.setAttribute("course", course);
                request.setAttribute("message", "File Media added successfully.");
            } else {
                request.setAttribute("error", "Failed to add file media.");
                request.setAttribute("course", chapter.getCourse());
            }
        } else {
            request.setAttribute("error", lesson == null ? "Lesson not found." : "Chapter not found.");
            Course course = courseDAO.findById(Long.parseLong(request.getParameter("courseId")));
            request.setAttribute("course", course);
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void updateFileMedia(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long fileMediaId = Long.parseLong(request.getParameter("fileMediaId"));
        String lessonId = request.getParameter("lessonId");
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String fileUrl = request.getParameter("fileUrl");

        FileMedia fileMedia = fileMediaDAO.findById(fileMediaId);
        Lesson lesson = lessonDAO.findById(lessonId);
        Chapter chapter = chapterDAO.findById(chapterId);
        if (fileMedia != null && lesson != null && chapter != null) {
            fileMedia.setFileName(fileName);
            fileMedia.setFileType(fileType);
            fileMedia.setFileUrl(fileUrl);
            fileMedia.setLesson(lesson);
            boolean success = fileMediaDAO.saveFileMedia(fileMedia);

            if (success) {
                lesson.setFileMedias(fileMediaDAO.findByLessonId(lessonId));
                chapter.setLessons(lessonDAO.findByChapterId(chapterId));
                Course course = chapter.getCourse();
                List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
                for (Chapter ch : chapters) {
                    List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                    for (Lesson l : lessons) {
                        l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                    }
                    ch.setLessons(lessons);
                }
                course.setChapters(chapters);
                request.setAttribute("course", course);
                request.setAttribute("message", "File Media updated successfully.");
            } else {
                request.setAttribute("error", "Failed to update file media.");
                request.setAttribute("course", chapter.getCourse());
            }
        } else {
            request.setAttribute("error", fileMedia == null ? "File Media not found." : (lesson == null ? "Lesson not found." : "Chapter not found."));
            Course course = courseDAO.findById(Long.parseLong(request.getParameter("courseId")));
            request.setAttribute("course", course);
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }

    private void deleteFileMedia(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long fileMediaId = Long.parseLong(request.getParameter("fileMediaId"));
        String lessonId = request.getParameter("lessonId");
        Long chapterId = Long.parseLong(request.getParameter("chapterId"));

        boolean success = fileMediaDAO.deleteFileMedia(fileMediaId);
        if (success) {
            Lesson lesson = lessonDAO.findById(lessonId);
            lesson.setFileMedias(fileMediaDAO.findByLessonId(lessonId));
            Chapter chapter = chapterDAO.findById(chapterId);
            chapter.setLessons(lessonDAO.findByChapterId(chapterId));
            Course course = chapter.getCourse();
            List<Chapter> chapters = chapterDAO.findByCourseId(course.getCourseId());
            for (Chapter ch : chapters) {
                List<Lesson> lessons = lessonDAO.findByChapterId(ch.getChapterId());
                for (Lesson l : lessons) {
                    l.setFileMedias(fileMediaDAO.findByLessonId(l.getLessonId()));
                }
                ch.setLessons(lessons);
            }
            course.setChapters(chapters);
            request.setAttribute("course", course);
            request.setAttribute("message", "File Media deleted successfully.");
        } else {
            request.setAttribute("error", "Failed to delete file media.");
            Chapter chapter = chapterDAO.findById(chapterId);
            request.setAttribute("course", chapter.getCourse());
        }

        request.setAttribute("categories", categoryDAO.findAll());
        request.getRequestDispatcher("/views/teacher/editCourse.jsp").forward(request, response);
    }
}