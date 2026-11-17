package hu.pte.mik.library.domain;

import java.util.Objects;

public class Member {

    private final int id;
    private final String name;
    private final String email;

    public Member(int id, String name, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("Member id must be positive.");
        }
        this.id = id;
        this.name = Texts.requireText(name, "Name");
        this.email = Texts.requireText(email, "Email");
        if (!this.email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email address is not valid.");
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Member other && id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d %s <%s>", id, name, email);
    }
}
