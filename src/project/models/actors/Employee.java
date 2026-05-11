package project.models.actors;

import java.util.*;
import project.enums.Role;
import project.models.others.Message;
import project.models.others.Request;
import project.enums.UrgencyLevel;

/**
 * Abstract base for all university employees (Teacher, Manager, Admin, TechSupport).
 * Employees can send/receive messages and create tech-support requests.
 */
public abstract class Employee extends User {
    private static final long serialVersionUID = 1L;
    private double salary;

    public Employee() {}
    public Employee(String id, String password, String firstName, String lastName, String email, double salary, Role role) {
        super(id, password, firstName, lastName, email, role);
        this.salary = salary;
    }

    public double getSalary() { return salary; }
    public void setSalary(double v) { this.salary = v; }

    @Override public String toString() {
        return super.toString() + "[salary=" + salary + "]";
    }
}
