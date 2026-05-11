package project.services;

import project.models.actors.Employee;
import project.storage.Database;

public class SalaryService {
    public void giveSalary(Employee employee, double amount) {
        employee.setSalary(employee.getSalary() + amount);
        Database.getInstance().saveUser(employee);
        System.out.println("[Salary] Paid " + amount + " to " + employee.getFullName() + " | New salary: " + employee.getSalary());
    }
}
