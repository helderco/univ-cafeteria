
package pt.uac.cafeteria.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Application core
 *
 * Central hub from where application scope objects come
 */
public class Application {



    /** Map with administrator accounts */
    private final Map<String, Administrator> administrators = new HashMap<String, Administrator>();

    /** Map with student accounts */
    private final Map<Integer, Student> students = new HashMap<Integer, Student>();

    /** Map with old students that no longer have an account */
    private final Map<Integer, Student> oldStudents = new HashMap<Integer, Student>();

    /**
     * Constructor
     *
     * A default administrator account is created at instantiation
     */
    public Application() {
        Administrator default_admin = new Administrator();
        administrators.put(default_admin.getUsername(), default_admin);
    }

    /**
     * Inserts a new administrator account in the application
     *
     * @param admin  Administrator object
     */
    public void addAdministrator(Administrator admin) {
        if (administrators.get(admin.getUsername()) != null) {
            throw new IllegalArgumentException(
                String.format("Conta de administrador com o username '%s' já ocupada.", admin.getUsername())
            );
        }
        administrators.put(admin.getUsername(), admin);
    }

    /**
     * Authenticates an Administrator
     *
     * @param username  Administrator username
     * @param password  Administrator password
     * @return  Administrator object, or null if invalid
     */
    public Administrator getAdministrator(String username, String password) {
        Administrator admin = administrators.get(username);

        if (admin != null && admin.isPasswordValid(password)) {
            return admin;
        }

        return null;
    }

    /**
     * Adds a Student to the application
     *
     * @param student  Student
     */
    public void addStudent(Student student) {
        students.put(new Integer(student.getId()), student);
    }

    /**
     * Gets a student.
     *
     * @param accountNumber  Student's account process number
     * @return  Student student object
     */
    Student getStudent(int accountNumber) {
        return students.get(new Integer(accountNumber));
    }

    /**
     * Authenticates a Student using his account.
     *
     * Three failed attempts blocks the account.
     *
     * @param accountNumber  Account process number
     * @param pinCode  Account pin code
     * @return  Student account object, or null if does not authenticate
     */
    public Student getStudent(int accountNumber, int pinCode) {
        Student student = getStudent(accountNumber);
        Account account = student.getAccount();

        if (account != null && account.authenticate(pinCode)) {
            return student;
        }

        return null;
    }

    /**
     * Deletes a student 
     *
     * Student gets moved to an historic of students (old students)
     *
     * @param accountNumber  Account or student process number
     */
    public void deleteStudent(int accountNumber) {
        Integer studentNumber = new Integer(accountNumber);
        Student student = students.get(studentNumber);

        if (student != null) {
            students.remove(studentNumber);
            oldStudents.put(studentNumber, student);
        }
    }

    /**
     * Gets an old student from the historic
     *
     * @param studentNumber  Student process number
     * @return   Student object, or null if non-existent
     */
    public Student getOldStudent(int studentNumber) {
        return oldStudents.get(new Integer(studentNumber));
    }
}
