// DatabaseOperations.java

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    // ===== AUDIT LOG HELPER =====
    public static void logAuditEvent(
            String actorEmpId,
            String actorRole,
            String actionType,
            String entityType,
            String entityId,
            String description,
            String extraData) {

        String sql = "INSERT INTO audit_log (" +
                "actor_empid, actor_role, action_type, entity_type, entity_id, " +
                "description, extra_data, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, actorEmpId);
            pstmt.setString(2, actorRole);
            pstmt.setString(3, actionType);
            pstmt.setString(4, entityType);
            pstmt.setString(5, entityId);
            pstmt.setString(6, description);
            pstmt.setString(7, extraData);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    // ===== LOGIN METHODS =====
    public static String[] authenticateUser(String username, String password) {
        String sql = "SELECT emp_id, emp_name, designation, dept_code, dept_name, role FROM employees " +
                "WHERE emp_id = ? AND password_hash = MD5(?) AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String[] userData = new String[6];
                userData[0] = rs.getString("emp_id");
                userData[1] = rs.getString("emp_name");
                userData[2] = rs.getString("designation");
                userData[3] = rs.getString("dept_code");
                userData[4] = rs.getString("dept_name");
                userData[5] = rs.getString("role");
                return userData;
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    // ===== REQUEST METHODS =====
    public static boolean submitVehicleRequest(String empId, String fromLocation, String toLocation,
                                               String purpose, String natureOfWork, String beyondArea,
                                               String beyondLocation, String travelersType, int familyCount,
                                               String trainDetails, String contactNumber) {

        String requestId = "REQ" + System.currentTimeMillis();
        String sql = "INSERT INTO vehicle_requests (" +
                "request_id, emp_id, from_location, to_location, purpose, " +
                "nature_of_work, beyond_area, beyond_location, travelers_type, family_count, " +
                "train_details, contact_number, status, hod_approval_status, coo_approval_status" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "'Pending', 'Pending', CASE WHEN ? = 'Yes' THEN 'Pending' ELSE 'Not Required' END" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            pstmt.setString(2, empId);
            pstmt.setString(3, fromLocation);
            pstmt.setString(4, toLocation);
            pstmt.setString(5, purpose);
            pstmt.setString(6, natureOfWork);
            pstmt.setString(7, beyondArea);
            pstmt.setString(8, beyondLocation);
            pstmt.setString(9, travelersType);
            pstmt.setInt(10, familyCount);
            pstmt.setString(11, trainDetails);
            pstmt.setString(12, contactNumber);
            pstmt.setString(13, beyondArea);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logAuditEvent(
                        empId,
                        "Employee",
                        "CREATE_REQUEST",
                        "VEHICLE_REQUEST",
                        requestId,
                        "Vehicle request created from " + fromLocation + " to " + toLocation,
                        "purpose=" + purpose
                );
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Submit request error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === EMPLOYEE HISTORY: full data from DB ===
    public static List<String[]> getEmployeeRequests(String empId) {
        List<String[]> requests = new ArrayList<>();

        String sql = "SELECT request_id, request_date, from_location, to_location, purpose, " +
                "status, hod_approval_status, coo_approval_status, " +
                "vehicle_name, vehicle_number, driver_name, driver_contact, " +
                "trip_completed, contact_number " +
                "FROM vehicle_requests " +
                "WHERE emp_id = ? " +
                "ORDER BY request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] requestData = new String[14];
                requestData[0] = rs.getString("request_id");
                requestData[1] = rs.getTimestamp("request_date") != null
                        ? rs.getTimestamp("request_date").toString()
                        : null;
                requestData[2] = rs.getString("from_location");
                requestData[3] = rs.getString("to_location");
                requestData[4] = rs.getString("purpose");
                requestData[5] = rs.getString("status");
                requestData[6] = rs.getString("hod_approval_status");
                requestData[7] = rs.getString("coo_approval_status");
                requestData[8] = rs.getString("vehicle_name");
                requestData[9] = rs.getString("vehicle_number");
                requestData[10] = rs.getString("driver_name");
                requestData[11] = rs.getString("driver_contact");
                requestData[12] = rs.getBoolean("trip_completed") ? "1" : "0";
                requestData[13] = rs.getString("contact_number");
                requests.add(requestData);
            }
        } catch (SQLException e) {
            System.err.println("Get employee requests error: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    // ===== ADMIN METHODS =====
    public static List<String[]> getAllRequests() {
        List<String[]> requests = new ArrayList<>();
        String sql = "SELECT vr.request_id, vr.emp_id, e.emp_name, vr.request_date, " +
                "vr.from_location, vr.to_location, vr.purpose, vr.status, " +
                "vr.hod_approval_status, vr.coo_approval_status, vr.vehicle_name, vr.driver_name " +
                "FROM vehicle_requests vr JOIN employees e ON vr.emp_id = e.emp_id " +
                "ORDER BY vr.request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] requestData = new String[12];
                requestData[0] = rs.getString("request_id");
                requestData[1] = rs.getString("emp_id");
                requestData[2] = rs.getString("emp_name");
                requestData[3] = rs.getTimestamp("request_date").toString();
                requestData[4] = rs.getString("from_location");
                requestData[5] = rs.getString("to_location");
                requestData[6] = rs.getString("purpose");
                requestData[7] = rs.getString("status");
                requestData[8] = rs.getString("hod_approval_status");
                requestData[9] = rs.getString("coo_approval_status");
                requestData[10] = rs.getString("vehicle_name");
                requestData[11] = rs.getString("driver_name");
                requests.add(requestData);
            }
        } catch (SQLException e) {
            System.err.println("Get all requests error: " + e.getMessage());
        }
        return requests;
    }

    // ===== HOD METHODS (FIXED) =====
    public static List<VehicleRequest> getRequestsForHOD() {
        List<VehicleRequest> list = new ArrayList<>();

        String sql =
                "SELECT vr.request_id, vr.emp_id, e.emp_name, vr.request_date, " +
                        "vr.from_location, vr.to_location, vr.purpose, vr.status, " +
                        "vr.hod_approval_status, vr.coo_approval_status, vr.vehicle_name, " +
                        "vr.vehicle_number, vr.driver_name, vr.driver_contact, " +
                        "vr.pickup_datetime, vr.dropoff_datetime, vr.trip_completed, " +
                        "vr.nature_of_work, vr.beyond_area, vr.beyond_location, " +
                        "vr.travelers_type, vr.family_count, vr.train_details, vr.contact_number " +
                        "FROM vehicle_requests vr " +
                        "JOIN employees e ON vr.emp_id = e.emp_id " +
                        "WHERE vr.hod_approval_status = 'Pending' " +
                        "ORDER BY vr.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                VehicleRequest r = new VehicleRequest(
                        rs.getString("request_id"),
                        rs.getString("emp_id")
                );

                r.setEmployeeName(rs.getString("emp_name"));
                r.setRequestDate(rs.getTimestamp("request_date"));
                r.setFromLocation(rs.getString("from_location"));
                r.setToLocation(rs.getString("to_location"));
                r.setPurpose(rs.getString("purpose"));
                r.setStatus(rs.getString("status"));
                r.setHodApprovalStatus(rs.getString("hod_approval_status"));
                r.setCooApprovalStatus(rs.getString("coo_approval_status"));
                r.setVehicleName(rs.getString("vehicle_name"));
                r.setVehicleNumber(rs.getString("vehicle_number"));
                r.setDriverName(rs.getString("driver_name"));
                r.setDriverContact(rs.getString("driver_contact"));

                Timestamp p = rs.getTimestamp("pickup_datetime");
                Timestamp d = rs.getTimestamp("dropoff_datetime");
                if (p != null) {
                    r.setPickupDateTime(new java.util.Date(p.getTime()));
                }
                if (d != null) {
                    r.setDropoffDateTime(new java.util.Date(d.getTime()));
                }

                r.setTripCompleted(rs.getBoolean("trip_completed"));
                r.setNatureOfWork(rs.getString("nature_of_work"));
                r.setBeyondArea(rs.getString("beyond_area"));
                r.setBeyondLocation(rs.getString("beyond_location"));
                r.setTravelersType(rs.getString("travelers_type"));
                r.setFamilyCount(rs.getInt("family_count"));
                r.setTrainDetails(rs.getString("train_details"));
                r.setContactNumber(rs.getString("contact_number"));

                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Get HOD requests error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // ===== COO METHODS =====
    public static List<VehicleRequest> getRequestsForCOO() {
        List<VehicleRequest> list = new ArrayList<>();
        String sql =
                "SELECT vr.request_id, vr.emp_id, e.emp_name, vr.request_date, " +
                        "vr.from_location, vr.to_location, vr.purpose, vr.status, " +
                        "vr.hod_approval_status, vr.coo_approval_status, " +
                        "vr.vehicle_name, vr.vehicle_number, vr.driver_name, vr.driver_contact, " +
                        "vr.pickup_datetime, vr.dropoff_datetime, vr.trip_completed, " +
                        "vr.nature_of_work, vr.beyond_area, vr.beyond_location, " +
                        "vr.travelers_type, vr.family_count, vr.train_details, vr.contact_number " +
                        "FROM vehicle_requests vr " +
                        "JOIN employees e ON vr.emp_id = e.emp_id " +
                        "WHERE vr.coo_approval_status <> 'Not Required' " +
                        "ORDER BY vr.request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                VehicleRequest r = new VehicleRequest(rs.getString("request_id"),
                        rs.getString("emp_id"));
                r.setEmployeeName(rs.getString("emp_name"));
                r.setRequestDate(rs.getTimestamp("request_date"));
                r.setFromLocation(rs.getString("from_location"));
                r.setToLocation(rs.getString("to_location"));
                r.setPurpose(rs.getString("purpose"));
                r.setStatus(rs.getString("status"));
                r.setHodApprovalStatus(rs.getString("hod_approval_status"));
                r.setCooApprovalStatus(rs.getString("coo_approval_status"));
                r.setVehicleName(rs.getString("vehicle_name"));
                r.setVehicleNumber(rs.getString("vehicle_number"));
                r.setDriverName(rs.getString("driver_name"));
                r.setDriverContact(rs.getString("driver_contact"));
                Timestamp p = rs.getTimestamp("pickup_datetime");
                Timestamp d = rs.getTimestamp("dropoff_datetime");
                if (p != null) r.setPickupDateTime(new java.util.Date(p.getTime()));
                if (d != null) r.setDropoffDateTime(new java.util.Date(d.getTime()));
                r.setTripCompleted(rs.getBoolean("trip_completed"));
                r.setNatureOfWork(rs.getString("nature_of_work"));
                r.setBeyondArea(rs.getString("beyond_area"));
                r.setBeyondLocation(rs.getString("beyond_location"));
                r.setTravelersType(rs.getString("travelers_type"));
                r.setFamilyCount(rs.getInt("family_count"));
                r.setTrainDetails(rs.getString("train_details"));
                r.setContactNumber(rs.getString("contact_number"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Get COO requests error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public static boolean updateRequestStatus(String requestId, String newStatus,
                                              String hodStatus, String cooStatus,
                                              String approverId, String approverRole) {

        String sql = "UPDATE vehicle_requests SET status = ?, " +
                "hod_approval_status = CASE WHEN ? IS NOT NULL THEN ? ELSE hod_approval_status END, " +
                "coo_approval_status = CASE WHEN ? IS NOT NULL THEN ? ELSE coo_approval_status END, " +
                "hod_approval_date = CASE WHEN ? = 'HOD' THEN NOW() ELSE hod_approval_date END, " +
                "coo_approval_date = CASE WHEN ? = 'COO' THEN NOW() ELSE coo_approval_date END, " +
                "hod_approver_id = CASE WHEN ? = 'HOD' THEN ? ELSE hod_approver_id END, " +
                "coo_approver_id = CASE WHEN ? = 'COO' THEN ? ELSE coo_approver_id END " +
                "WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, hodStatus);
            pstmt.setString(3, hodStatus);
            pstmt.setString(4, cooStatus);
            pstmt.setString(5, cooStatus);
            pstmt.setString(6, approverRole);
            pstmt.setString(7, approverRole);
            pstmt.setString(8, approverRole);
            pstmt.setString(9, approverId);
            pstmt.setString(10, approverRole);
            pstmt.setString(11, approverId);
            pstmt.setString(12, requestId);

            int rows = pstmt.executeUpdate();
            if (rows > 0 && ("HOD".equals(approverRole) || "COO".equals(approverRole))) {
                logApprovalHistory(requestId, approverId, approverRole, newStatus,
                        hodStatus != null ? hodStatus : cooStatus, "Status updated");
                logAuditEvent(
                        approverId,
                        approverRole,
                        "UPDATE_REQUEST_STATUS",
                        "VEHICLE_REQUEST",
                        requestId,
                        "Status changed to " + newStatus,
                        "hodStatus=" + hodStatus + ", cooStatus=" + cooStatus
                );
            }

            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Update status error: " + e.getMessage());
            return false;
        }
    }

    private static void logApprovalHistory(String requestId, String approverId,
                                           String approverRole, String newStatus,
                                           String decision, String comments) {

        String sql = "INSERT INTO approval_history (request_id, approver_id, approver_role, " +
                "new_status, approval_decision, comments) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            pstmt.setString(2, approverId);
            pstmt.setString(3, approverRole);
            pstmt.setString(4, newStatus);
            pstmt.setString(5, decision);
            pstmt.setString(6, comments);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Log approval error: " + e.getMessage());
        }
    }

    // ===== GARAGE METHODS =====
    public static List<String[]> getGarageRequests() {
        List<String[]> requests = new ArrayList<>();
        String sql = "SELECT vr.request_id, vr.emp_id, e.emp_name, e.designation, e.dept_name, " +
                "vr.from_location, vr.to_location, vr.purpose, vr.status, " +
                "vr.vehicle_name, vr.vehicle_number, vr.driver_name, vr.driver_contact, " +
                "vr.pickup_datetime, vr.dropoff_datetime, vr.trip_completed, " +
                "vr.contact_number AS requester_contact " +
                "FROM vehicle_requests vr " +
                "JOIN employees e ON vr.emp_id = e.emp_id " +
                "WHERE vr.status IN (" +
                " 'Waiting for COO Approval'," +
                " 'Approved by HOD'," +
                " 'Approved (HOD & COO)'," +
                " 'Assigned'," +
                " 'Completed'" +
                ") " +
                "ORDER BY " +
                "CASE vr.status " +
                " WHEN 'Approved (HOD & COO)' THEN 1 " +
                " WHEN 'Waiting for COO Approval' THEN 2 " +
                " WHEN 'Approved by HOD' THEN 3 " +
                " WHEN 'Assigned' THEN 4 " +
                " WHEN 'Completed' THEN 5 " +
                "END, vr.request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] requestData = new String[16];
                requestData[0] = rs.getString("request_id");
                requestData[1] = rs.getString("emp_id");
                requestData[2] = rs.getString("emp_name");
                requestData[3] = rs.getString("designation");
                requestData[4] = rs.getString("dept_name");
                requestData[5] = rs.getString("from_location");
                requestData[6] = rs.getString("to_location");
                requestData[7] = rs.getString("purpose");
                requestData[8] = rs.getString("status");
                requestData[9] = rs.getString("vehicle_name");
                requestData[10] = rs.getString("vehicle_number");
                requestData[11] = rs.getString("driver_name");
                requestData[12] = rs.getString("driver_contact");
                requestData[13] = rs.getTimestamp("pickup_datetime") != null
                        ? rs.getTimestamp("pickup_datetime").toString() : null;
                requestData[14] = rs.getTimestamp("dropoff_datetime") != null
                        ? rs.getTimestamp("dropoff_datetime").toString() : null;
                requestData[15] = rs.getBoolean("trip_completed") ? "1" : "0";
                requests.add(requestData);
            }
        } catch (SQLException e) {
            System.err.println("Get garage requests error: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    public static boolean updateGarageAssignment(String requestId, String vehicleName, String vehicleNumber,
                                                 String driverName, String driverContact,
                                                 Timestamp pickupDateTime, Timestamp dropoffDateTime,
                                                 boolean tripCompleted, String assignedBy) {

        String sql = "UPDATE vehicle_requests SET " +
                "vehicle_name = ?, vehicle_number = ?, driver_name = ?, driver_contact = ?, " +
                "pickup_datetime = ?, dropoff_datetime = ?, trip_completed = ?, " +
                "status = CASE WHEN ? = TRUE THEN 'Completed' ELSE 'Assigned' END " +
                "WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicleName);
            pstmt.setString(2, vehicleNumber);
            pstmt.setString(3, driverName);
            pstmt.setString(4, driverContact);
            pstmt.setTimestamp(5, pickupDateTime);
            pstmt.setTimestamp(6, dropoffDateTime);
            pstmt.setBoolean(7, tripCompleted);
            pstmt.setBoolean(8, tripCompleted);
            pstmt.setString(9, requestId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                updateVehicleAssignment(requestId, vehicleName, vehicleNumber,
                        driverName, driverContact, pickupDateTime,
                        dropoffDateTime, tripCompleted, assignedBy);

                logAuditEvent(
                        assignedBy,
                        "Garage",
                        tripCompleted ? "COMPLETE_TRIP" : "ASSIGN_VEHICLE",
                        "VEHICLE_REQUEST",
                        requestId,
                        tripCompleted ? "Trip marked completed" : "Vehicle assigned",
                        "vehicle=" + vehicleNumber + ", driver=" + driverName
                );
            }

            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Update garage assignment error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void updateVehicleAssignment(String requestId, String vehicleName,
                                                String vehicleNumber, String driverName,
                                                String driverContact, Timestamp pickupDateTime,
                                                Timestamp dropoffDateTime, boolean tripCompleted,
                                                String assignedBy) {

        String checkSql = "SELECT COUNT(*) FROM vehicle_assignments WHERE request_id = ?";
        String insertSql = "INSERT INTO vehicle_assignments (request_id, vehicle_name, vehicle_number, " +
                "driver_name, driver_contact, pickup_datetime, dropoff_datetime, " +
                "trip_completed, assigned_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE vehicle_assignments SET vehicle_name = ?, vehicle_number = ?, " +
                "driver_name = ?, driver_contact = ?, pickup_datetime = ?, " +
                "dropoff_datetime = ?, trip_completed = ?, assigned_by = ?, " +
                "assignment_date = NOW(), completion_date = CASE WHEN ? = TRUE THEN NOW() ELSE NULL END " +
                "WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean assignmentExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, requestId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    assignmentExists = rs.getInt(1) > 0;
                }
            }

            String sql = assignmentExists ? updateSql : insertSql;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (assignmentExists) {
                    pstmt.setString(1, vehicleName);
                    pstmt.setString(2, vehicleNumber);
                    pstmt.setString(3, driverName);
                    pstmt.setString(4, driverContact);
                    pstmt.setTimestamp(5, pickupDateTime);
                    pstmt.setTimestamp(6, dropoffDateTime);
                    pstmt.setBoolean(7, tripCompleted);
                    pstmt.setString(8, assignedBy);
                    pstmt.setBoolean(9, tripCompleted);
                    pstmt.setString(10, requestId);
                } else {
                    pstmt.setString(1, requestId);
                    pstmt.setString(2, vehicleName);
                    pstmt.setString(3, vehicleNumber);
                    pstmt.setString(4, driverName);
                    pstmt.setString(5, driverContact);
                    pstmt.setTimestamp(6, pickupDateTime);
                    pstmt.setTimestamp(7, dropoffDateTime);
                    pstmt.setBoolean(8, tripCompleted);
                    pstmt.setString(9, assignedBy);
                }
                pstmt.executeUpdate();
            }

            updateVehicleAvailability(vehicleNumber, !tripCompleted);
        } catch (SQLException e) {
            System.err.println("Update vehicle assignment error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateVehicleAvailability(String vehicleNumber, boolean isAssigned) {
        String sql = "UPDATE vehicles SET is_available = ? WHERE vehicle_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, !isAssigned);
            pstmt.setString(2, vehicleNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update vehicle availability error: " + e.getMessage());
        }
    }

    // ===== DETAILS =====
    public static String[] getVehicleRequestDetails(String requestId) {
        String sql = "SELECT vr.request_id, vr.emp_id, e.emp_name, e.designation, e.dept_name, " +
                "vr.from_location, vr.to_location, vr.purpose, vr.status, " +
                "vr.vehicle_name, vr.vehicle_number, vr.driver_name, vr.driver_contact, " +
                "vr.pickup_datetime, vr.dropoff_datetime, vr.trip_completed, " +
                "vr.contact_number AS requester_contact " +
                "FROM vehicle_requests vr " +
                "JOIN employees e ON vr.emp_id = e.emp_id " +
                "WHERE vr.request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String[] requestData = new String[17];
                requestData[0] = rs.getString("request_id");
                requestData[1] = rs.getString("emp_id");
                requestData[2] = rs.getString("emp_name");
                requestData[3] = rs.getString("designation");
                requestData[4] = rs.getString("dept_name");
                requestData[5] = rs.getString("from_location");
                requestData[6] = rs.getString("to_location");
                requestData[7] = rs.getString("purpose");
                requestData[8] = rs.getString("status");
                requestData[9] = rs.getString("vehicle_name");
                requestData[10] = rs.getString("vehicle_number");
                requestData[11] = rs.getString("driver_name");
                requestData[12] = rs.getString("driver_contact");
                requestData[13] = rs.getTimestamp("pickup_datetime") != null
                        ? rs.getTimestamp("pickup_datetime").toString() : null;
                requestData[14] = rs.getTimestamp("dropoff_datetime") != null
                        ? rs.getTimestamp("dropoff_datetime").toString() : null;
                requestData[15] = rs.getBoolean("trip_completed") ? "1" : "0";
                requestData[16] = rs.getString("requester_contact");
                return requestData;
            }
        } catch (SQLException e) {
            System.err.println("Get request details error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ===== NEW: BULK EMPLOYEE CSV IMPORT =====
    // CSV format (you can keep a header; it will be skipped if it starts with 'emp_id'):
    // emp_id, emp_name, designation, dept_code, dept_name, password_plain, role, is_active
    public static int bulkInsertEmployeesFromCsv(java.io.File csvFile) {
        int processed = 0;

        String sql =
                "INSERT INTO employees " +
                        "(emp_id, emp_name, designation, dept_code, dept_name, password_hash, role, is_active) " +
                        "VALUES (?, ?, ?, ?, ?, MD5(?), ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "emp_name = VALUES(emp_name), " +
                        "designation = VALUES(designation), " +
                        "dept_code = VALUES(dept_code), " +
                        "dept_name = VALUES(dept_name), " +
                        "password_hash = VALUES(password_hash), " +
                        "role = VALUES(role), " +
                        "is_active = VALUES(is_active)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {

            conn.setAutoCommit(false);

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toLowerCase().startsWith("emp_id")) {
                    // header row
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 8) {
                    // invalid row, skip
                    continue;
                }

                String empId = parts[0].trim();
                String empName = parts[1].trim();
                String designation = parts[2].trim();
                String deptCode = parts[3].trim();
                String deptName = parts[4].trim();
                String passwordPlain = parts[5].trim();
                String role = parts[6].trim();
                String activeStr = parts[7].trim();
                boolean isActive = !"0".equals(activeStr) && !"false".equalsIgnoreCase(activeStr);

                ps.setString(1, empId);
                ps.setString(2, empName);
                ps.setString(3, designation);
                ps.setString(4, deptCode.isEmpty() ? null : deptCode);
                ps.setString(5, deptName.isEmpty() ? null : deptName);
                ps.setString(6, passwordPlain);
                ps.setString(7, role.isEmpty() ? "Employee" : role);
                ps.setBoolean(8, isActive);

                ps.addBatch();
                processed++;
            }

            ps.executeBatch();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return processed;
    }
}