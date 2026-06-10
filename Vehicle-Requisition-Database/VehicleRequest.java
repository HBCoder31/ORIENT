// VehicleRequest.java

import java.text.SimpleDateFormat;
import java.util.Date;

public class VehicleRequest {

    private final String requestId;
    private final String empNo;

    private Date requestDate;
    private String fromLocation;
    private String toLocation;
    private String purpose;
    private String status;
    private String hodApprovalStatus;
    private String cooApprovalStatus;
    private String vehicleName;
    private String vehicleNumber;
    private String driverName;
    private String driverContact;
    private boolean tripCompleted;

    // Timing fields for garage management
    private Date pickupDateTime;
    private Date dropoffDateTime;

    // Extra fields used for COO / beyond-area logic
    private String natureOfWork;
    private String beyondArea;
    private String beyondLocation;
    private String travelersType;
    private int familyCount;
    private String trainDetails;
    private String contactNumber;

    // Employee display
    private String employeeName;

    public VehicleRequest(String requestId, String empNo) {
        this.requestId = requestId;
        this.empNo = empNo;
        this.status = "Pending";
        this.hodApprovalStatus = "Pending";
        this.cooApprovalStatus = "Not Required";
        this.tripCompleted = false;
        this.requestDate = new Date();
        this.pickupDateTime = null;
        this.dropoffDateTime = null;
    }

    // Getters and setters
    public String getRequestId() { return requestId; }

    public String getEmpNo() { return empNo; }

    public Date getRequestDate() { return requestDate; }

    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }

    public String getFromLocation() { return fromLocation; }

    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }

    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public String getPurpose() { return purpose; }

    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getHodApprovalStatus() { return hodApprovalStatus; }

    public void setHodApprovalStatus(String hodApprovalStatus) { this.hodApprovalStatus = hodApprovalStatus; }

    public String getCooApprovalStatus() { return cooApprovalStatus; }

    public void setCooApprovalStatus(String cooApprovalStatus) { this.cooApprovalStatus = cooApprovalStatus; }

    public String getVehicleName() { return vehicleName; }

    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleNumber() { return vehicleNumber; }

    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getDriverName() { return driverName; }

    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverContact() { return driverContact; }

    public void setDriverContact(String driverContact) { this.driverContact = driverContact; }

    public boolean isTripCompleted() { return tripCompleted; }

    public void setTripCompleted(boolean tripCompleted) { this.tripCompleted = tripCompleted; }

    // Timing getters and setters
    public Date getPickupDateTime() { return pickupDateTime; }

    public void setPickupDateTime(Date pickupDateTime) { this.pickupDateTime = pickupDateTime; }

    public Date getDropoffDateTime() { return dropoffDateTime; }

    public void setDropoffDateTime(Date dropoffDateTime) { this.dropoffDateTime = dropoffDateTime; }

    // Extra COO / beyond-area fields
    public String getNatureOfWork() { return natureOfWork; }

    public void setNatureOfWork(String natureOfWork) { this.natureOfWork = natureOfWork; }

    public String getBeyondArea() { return beyondArea; }

    public void setBeyondArea(String beyondArea) { this.beyondArea = beyondArea; }

    public String getBeyondLocation() { return beyondLocation; }

    public void setBeyondLocation(String beyondLocation) { this.beyondLocation = beyondLocation; }

    public String getTravelersType() { return travelersType; }

    public void setTravelersType(String travelersType) { this.travelersType = travelersType; }

    public int getFamilyCount() { return familyCount; }

    public void setFamilyCount(int familyCount) { this.familyCount = familyCount; }

    public String getTrainDetails() { return trainDetails; }

    public void setTrainDetails(String trainDetails) { this.trainDetails = trainDetails; }

    public String getContactNumber() { return contactNumber; }

    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmployeeName() { return employeeName; }

    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    // Helper methods for formatted date/time strings
    public String getRequestDateString() {
        if (requestDate == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        return sdf.format(requestDate);
    }

    public String getPickupDateString() {
        if (pickupDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(pickupDateTime);
    }

    public String getPickupTimeString() {
        if (pickupDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(pickupDateTime);
    }

    public String getDropoffDateString() {
        if (dropoffDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(dropoffDateTime);
    }

    public String getDropoffTimeString() {
        if (dropoffDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(dropoffDateTime);
    }

    public String getFullPickupDateTime() {
        if (pickupDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(pickupDateTime);
    }

    public String getFullDropoffDateTime() {
        if (dropoffDateTime == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(dropoffDateTime);
    }
}
