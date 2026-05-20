package project.models.others;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A student organization with a designated leader and a list of members.
 * Students must submit a join request; the lead approves or rejects it.
 */
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String leadId;
    private List<String> memberIds = new ArrayList<>();

    public Organization() {}
    public Organization(String name, String leadId) {
        this.name = name;
        this.leadId = leadId;
        memberIds.add(leadId);
    }

    /**
     * Checks whether the given user is a member (including the lead).
     *
     * @param userId the user's ID to check
     * @return {@code true} if the user belongs to this organization
     */
    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }

    /**
     * Adds a user to the organization. Duplicates are silently ignored.
     *
     * @param userId the ID of the user to add
     */
    public void addMember(String userId) {
        if (!memberIds.contains(userId)) memberIds.add(userId);
    }

    /**
     * Removes a user from the organization.
     *
     * @param userId the ID of the user to remove
     */
    public void removeMember(String userId) { memberIds.remove(userId); }

    public String getId()              { return id; }
    public void setId(String id)       { this.id = id; }
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }
    public String getLeadId()          { return leadId; }
    public void setLeadId(String v)    { this.leadId = v; }
    public List<String> getMemberIds() {
        if (memberIds == null) memberIds = new ArrayList<>();
        return memberIds;
    }
    public void setMemberIds(List<String> v) { this.memberIds = v; }

    @Override public String toString() {
        return "[" + id + "] " + name + "  (members: " + getMemberIds().size() + ")";
    }
}
