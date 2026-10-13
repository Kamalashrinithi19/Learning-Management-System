package com.lms.config;

import com.lms.entity.*;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already seeded. Skipping.");
                return;
            }
            log.info("Seeding demo data...");

            // --- Users ---
            User admin = createUser("Admin User", "admin@lms.com", "admin123", Role.ADMIN);
            User instructor1 = createUser("Dr. Alice Johnson", "alice@lms.com", "alice123", Role.INSTRUCTOR);
            User instructor2 = createUser("Prof. Bob Smith", "bob@lms.com", "bob123", Role.INSTRUCTOR);
            User student1 = createUser("Charlie Brown", "charlie@lms.com", "charlie123", Role.STUDENT);
            User student2 = createUser("Diana Prince", "diana@lms.com", "diana123", Role.STUDENT);
            User student3 = createUser("Eve Adams", "eve@lms.com", "eve123", Role.STUDENT);

            // --- Courses ---
            Course javaCourse = createCourse("Java Programming Fundamentals",
                    "Learn Java from scratch — variables, OOP, collections, and more.",
                    instructor1, CourseStatus.PUBLISHED);

            Course webCourse = createCourse("Full-Stack Web Development",
                    "Build modern web apps using React, Spring Boot, and MySQL.",
                    instructor1, CourseStatus.PUBLISHED);

            Course dsCourse = createCourse("Data Structures & Algorithms",
                    "Master arrays, linked lists, trees, graphs and algorithm design.",
                    instructor2, CourseStatus.PUBLISHED);

            Course mlCourse = createCourse("Introduction to Machine Learning",
                    "Supervised, unsupervised learning, and neural networks basics.",
                    instructor2, CourseStatus.DRAFT);

            // --- Lessons ---
            addLesson(javaCourse, "Introduction to Java", "What is Java? History and features.",
                    "https://docs.oracle.com/javase/tutorial/", "Java is a high-level, class-based, object-oriented programming language.", 1);
            addLesson(javaCourse, "Variables and Data Types", "Primitive types, reference types, and type casting.",
                    null, "Java has 8 primitive data types: byte, short, int, long, float, double, boolean, char.", 2);
            addLesson(javaCourse, "Object-Oriented Programming", "Classes, objects, inheritance, polymorphism.",
                    null, "OOP principles: Encapsulation, Inheritance, Polymorphism, Abstraction.", 3);
            addLesson(javaCourse, "Collections Framework", "List, Set, Map and their implementations.",
                    "https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html",
                    "The Collections Framework provides a unified architecture for storing and manipulating groups of objects.", 4);

            addLesson(webCourse, "React Fundamentals", "Components, props, state, and hooks.",
                    "https://react.dev/", "React is a JavaScript library for building user interfaces.", 1);
            addLesson(webCourse, "Spring Boot Basics", "REST APIs with Spring Boot.",
                    null, "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications.", 2);
            addLesson(webCourse, "Database Design with MySQL", "Tables, relationships, and SQL queries.",
                    null, "MySQL is the world's most popular open-source database.", 3);

            addLesson(dsCourse, "Arrays and Strings", "Static arrays, dynamic arrays, and string manipulation.",
                    null, "Arrays store elements in contiguous memory locations.", 1);
            addLesson(dsCourse, "Linked Lists", "Singly and doubly linked lists, operations.",
                    null, "A linked list is a linear data structure where elements are stored in nodes.", 2);
            addLesson(dsCourse, "Trees and Graphs", "Binary trees, BST, BFS, DFS.",
                    null, "Trees are hierarchical data structures with a root node and child nodes.", 3);

            // --- Assignments ---
            Assignment javaAssignment1 = createAssignment(javaCourse, "Hello World Program",
                    "Write a Java program that prints Hello World and demonstrates basic I/O.",
                    LocalDateTime.now().plusDays(7), 100);
            Assignment javaAssignment2 = createAssignment(javaCourse, "OOP Mini Project",
                    "Create a simple Bank Account system using OOP principles.",
                    LocalDateTime.now().plusDays(14), 150);
            Assignment webAssignment = createAssignment(webCourse, "React Todo App",
                    "Build a fully functional Todo application using React hooks.",
                    LocalDateTime.now().plusDays(10), 100);
            Assignment dsAssignment = createAssignment(dsCourse, "Sorting Algorithms",
                    "Implement Bubble Sort, Merge Sort, and Quick Sort and compare their performance.",
                    LocalDateTime.now().minusDays(2), 100); // past deadline for testing

            // --- Enrollments ---
            Enrollment e1 = createEnrollment(student1, javaCourse);
            Enrollment e2 = createEnrollment(student1, webCourse);
            Enrollment e3 = createEnrollment(student2, javaCourse);
            Enrollment e4 = createEnrollment(student2, dsCourse);
            Enrollment e5 = createEnrollment(student3, webCourse);
            Enrollment e6 = createEnrollment(student3, dsCourse);

            // --- Submissions ---
            Submission sub1 = createSubmission(javaAssignment1, student1,
                    "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }",
                    SubmissionStatus.GRADED);
            Submission sub2 = createSubmission(javaAssignment1, student2,
                    "class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }",
                    SubmissionStatus.SUBMITTED);
            Submission sub3 = createSubmission(webAssignment, student1,
                    "Created a React Todo app with useState for managing tasks. Added add, delete, and toggle complete features.",
                    SubmissionStatus.SUBMITTED);
            Submission sub4 = createSubmission(dsAssignment, student2,
                    "Implemented all three sorting algorithms with time complexity analysis.",
                    SubmissionStatus.LATE);

            // --- Grades ---
            createGrade(sub1, 92.0, "Excellent work! Clean code and proper formatting.", instructor1);

            // --- Notifications ---
            createNotification(student1, "Welcome to LMS!", "Welcome Charlie! Start exploring your courses.", NotificationType.INFO);
            createNotification(student1, "New Assignment Posted", "A new assignment has been posted in Java Programming.", NotificationType.ASSIGNMENT);
            createNotification(student1, "Assignment Graded", "Your Hello World submission has been graded: 92/100", NotificationType.GRADE);
            createNotification(student2, "Welcome to LMS!", "Welcome Diana! Start exploring your courses.", NotificationType.INFO);
            createNotification(student3, "Welcome to LMS!", "Welcome Eve! Start exploring your courses.", NotificationType.INFO);
            createNotification(instructor1, "New Enrollment", "Charlie Brown enrolled in Java Programming.", NotificationType.ENROLLMENT);

            log.info("✅ Demo data seeded successfully!");
            log.info("Demo accounts:");
            log.info("  Admin: admin@lms.com / admin123");
            log.info("  Instructor: alice@lms.com / alice123");
            log.info("  Instructor: bob@lms.com / bob123");
            log.info("  Student: charlie@lms.com / charlie123");
            log.info("  Student: diana@lms.com / diana123");
            log.info("  Student: eve@lms.com / eve123");
        };
    }

    private User createUser(String name, String email, String password, Role role) {
        User user = User.builder()
                .fullName(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private Course createCourse(String title, String description, User instructor, CourseStatus status) {
        Course course = Course.builder()
                .title(title)
                .description(description)
                .instructor(instructor)
                .status(status)
                .build();
        return courseRepository.save(course);
    }

    private void addLesson(Course course, String title, String description,
                           String contentUrl, String textContent, int seq) {
        Lesson lesson = Lesson.builder()
                .course(course)
                .title(title)
                .description(description)
                .contentUrl(contentUrl)
                .textContent(textContent)
                .sequenceNumber(seq)
                .build();
        lessonRepository.save(lesson);
    }

    private Assignment createAssignment(Course course, String title, String description,
                                         LocalDateTime deadline, int maxMarks) {
        Assignment assignment = Assignment.builder()
                .course(course)
                .title(title)
                .description(description)
                .deadline(deadline)
                .maxMarks(maxMarks)
                .build();
        return assignmentRepository.save(assignment);
    }

    private Enrollment createEnrollment(User student, Course course) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        return enrollmentRepository.save(enrollment);
    }

    private Submission createSubmission(Assignment assignment, User student,
                                         String text, SubmissionStatus status) {
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submissionText(text)
                .submittedAt(LocalDateTime.now().minusDays(1))
                .status(status)
                .build();
        return submissionRepository.save(submission);
    }

    private void createGrade(Submission submission, double marks, String feedback, User gradedBy) {
        Grade grade = Grade.builder()
                .submission(submission)
                .marks(marks)
                .feedback(feedback)
                .gradedBy(gradedBy)
                .gradedAt(LocalDateTime.now().minusHours(12))
                .build();
        gradeRepository.save(grade);
    }

    private void createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }
}
