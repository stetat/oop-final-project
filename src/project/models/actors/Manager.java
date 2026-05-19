package project.models.actors;

import project.models.enums.ManagerType;
import project.models.enums.Role;

public class Manager extends Employee {
    private static final long serialVersionUID = 1L;
    private ManagerType type;

    public Manager() {}
    public Manager(String id, String password, String firstName, String lastName, String email, double salary, ManagerType type) {
        super(id, password, firstName, lastName, email, salary, Role.MANAGER);
        this.type = type;
    }

    public ManagerType getType() { return type; }
    public void setType(ManagerType v) { this.type = v; }

    @Override public String toString() {
        return "Manager[id=" + getId() + ", name=" + getFullName() + ", type=" + type + "]";
    }
}
