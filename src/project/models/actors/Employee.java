package project.models.actors;

import project.models.enums.Role;

/**
 * Abstract base for all salaried university staff (Teacher, Manager, Admin, TechSupport).
 * Carries a salary field; subclasses add role-specific behaviour.
 */
public abstract class Employee extends User {
    private static final long serialVersionUID = 1L;
    private double salary;

    /** No-arg constructor required for Java serialization. */
    public Employee() {}

    /**
     * @param id        unique login ID
     * @param password  plain-text password
     * @param firstName first name
     * @param lastName  last name
     * @param email     university e-mail
     * @param salary    starting annual salary in KZT
     * @param role      the employee's system role
     */
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
