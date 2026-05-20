package project.services;

import project.models.actors.Employee;
import project.storage.Database;

/** Handles salary payments for employees. */
public class SalaryService {

    /**
     * Adds {@code amount} to the employee's current salary and persists the change.
     *
     * @param employee the employee to pay
     * @param amount   the amount to add
     */
    public void giveSalary(Employee employee, double amount) {
        employee.setSalary(employee.getSalary() + amount);
        Database.getInstance().saveUser(employee);
        System.out.println("[Salary] Paid " + amount + " to " + employee.getFullName() + " | New salary: " + employee.getSalary());
    }
}
