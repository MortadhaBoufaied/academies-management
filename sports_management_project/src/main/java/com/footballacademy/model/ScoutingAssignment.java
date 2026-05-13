package com.footballacademy.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="scouting_assignments", indexes={@Index(name="idx_assignment_academy", columnList="academy_id"),@Index(name="idx_assignment_scouter", columnList="scouter_id"),@Index(name="idx_assignment_division", columnList="division_id"),@Index(name="idx_assignment_status", columnList="status")})
public class ScoutingAssignment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="academy_id") @JsonIgnore private Academy academy;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="scouter_id") @JsonIgnore private Scouter scouter;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="division_id") @JsonIgnore private Division division;
 private LocalDateTime assignedAt=LocalDateTime.now();
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ScoutingStatus status=ScoutingStatus.IN_REVIEW;
 @Column(length=1000) private String notes;
 public boolean isActive(){return status==ScoutingStatus.IN_REVIEW||status==ScoutingStatus.SHORTLISTED;}
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public Academy getAcademy(){return academy;} public void setAcademy(Academy academy){this.academy=academy;} public Scouter getScouter(){return scouter;} public void setScouter(Scouter scouter){this.scouter=scouter;} public Division getDivision(){return division;} public void setDivision(Division division){this.division=division;} public LocalDateTime getAssignedAt(){return assignedAt;} public void setAssignedAt(LocalDateTime assignedAt){this.assignedAt=assignedAt;} public ScoutingStatus getStatus(){return status;} public void setStatus(ScoutingStatus status){this.status=status;} public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;}
}
