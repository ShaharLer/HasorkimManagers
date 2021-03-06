package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class Report implements java.io.Serializable {


    private String id;
    private String reporterName;
    private int incrementalReportId;
    private String status;
    private long startTime;
    private String address;
    private String freeText;
    private String phoneNumber;
    private String extraPhoneNumber;
    private String photoPath;

    private String assignedScanner;

    private String scannerOnTheWay;

    private int availableScanners;

    private Map<String, String> potentialScanners = new HashMap<>();


    private String cancellationText;
    private String cancellationUserType;
    private String userId;

    private boolean hasSimilarReports;
    private boolean isDogWithReporter;
    private String imageUrl;

    private boolean scannerEnlisted;
    private String managerInCharge;

    private double latitude;
    private double longitude;

    private String distance;
    private String duration;

    private int nextIncrementalId;
    private static final String TAG = "Report";
    private int distancevalue;

    private void changePotentialScanners(String userId, int change) {
        if (change == 0) potentialScanners.remove(userId);
        else if (change == 1) potentialScanners.put(userId, this.getDuration());

        availableScanners = potentialScanners.size();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);

        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("availableScanners", availableScanners);
        reportMap.put("potentialScanners", potentialScanners);

        reportsRef.updateChildren(reportMap);
    }

    public void addToPotentialScanners(String userId) {
        this.changePotentialScanners(userId, 1);
    }


    public void subtrectFromPotentialScanners(String userId) {
        this.changePotentialScanners(userId, 0);
    }


    public boolean isCurrentUserScannerEnlisted(String userId) {
        return potentialScanners.containsKey(userId);
    }

    public Map<String, String> getPotentialScanners() {
        return potentialScanners;
    }

    public void setPotentialScanners(Map<String, String> potentialScanners) {
        this.potentialScanners = potentialScanners;
        availableScanners = potentialScanners.size();
    }


    public void addPotentialScanner(String scannerId, String duration) {
        this.potentialScanners.put(scannerId, duration);
    }

    public int getPotentialScannersSize() {
        return this.potentialScanners.size();
    }


    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)

        //this.buildPotentialScanners();
    }


    public void setIsDogWithReporter(boolean isDogWithReporter) {
        this.isDogWithReporter = isDogWithReporter;
    }

    public boolean getIsDogWithReporter() {
        return isDogWithReporter;
    }


    public String getId() {
        return id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getFreeText() {
        return freeText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getExtraPhoneNumber() {
        return extraPhoneNumber;
    }

    public String getAssignedScanner() {
        if (assignedScanner == null) return "";
        return assignedScanner;
    }


    public String getStatus() {

        if (Objects.equals(this.status, "NEW")) {
            if (isScannerEnlisted()) return "SCANNER_ENLISTED";
            //if (potentialScanners.size() > 0) return "SCANNER_ENLISTED";
            return "NEW";
        }

        return this.status;
    }

    private String previousStatus;

    public void setStatus(String status) {
        this.previousStatus = this.status;
        this.status = status;
    }


    public long getStartTime() {
        return this.startTime;
    }

    public String getStartTimeAsString() {
        Format format = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return format.format(new Date(-this.startTime));
    }

    public String getAddress() {
        return this.address;
    }

    public String getUserId() {
        return userId;
    }

    public int getAvailableScanners() {
        return this.availableScanners;
    }

    public void setAvailableScanners(int availableScanners) {
        this.availableScanners = availableScanners;
    }

    public String getCancellationText() {
        return cancellationText;
    }

    public int getIncrementalReportId() {
        return incrementalReportId;
    }

    /*
    public void persistReport(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports");
        reportsRef.push().setValue(this);
    }*/

    public void setExtraPhoneNumber(String extraPhoneNumber) {
        this.extraPhoneNumber = extraPhoneNumber;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMoreInformation(String moreInformation) {
        this.freeText = moreInformation;
    }

    public void setId(String id) {
        this.id = id;
        buildPotentialScanners();
    }

    private void buildPotentialScanners() {
        if (this.id != null) {
            DatabaseReference reportsRef = FirebaseDatabase.getInstance()
                    .getReference("reports").child(this.getId()).child("potentialScanners");

            reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                    for (DataSnapshot userId : contactChildren) {
                        addPotentialScanner(userId.getKey(), userId.getValue().toString());
                    }

                    availableScanners = potentialScanners.size();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        } else {
            Log.e(TAG, "id is null");
        }
    }

    public void setStartTime() {
        this.startTime = -Calendar.getInstance().getTime().getTime();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCancellationText(String cancellationText) {
        this.cancellationText = cancellationText;
    }

    @Exclude
    public void setIncrementalReportId() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("reports").orderByChild("incrementalReportId").limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();

                for (DataSnapshot report : contactChildren) {
                    Report lastReport = report.getValue(Report.class);
                    nextIncrementalId = lastReport.getIncrementalReportId() + 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        this.incrementalReportId = nextIncrementalId;
    }

    public void logReportUpdateStatus(String status) {

        User user = getUser();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS")
                .format(new java.util.Date());

        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("status_logs")
                .child(this.getId()).child(timeStamp);

        logRef.child("report").setValue(this);
        logRef.child("user").setValue(user);
        logRef.child("oldStatus").setValue(this.previousStatus);
        logRef.child("newStatus").setValue(status);
    }

    public void reportUpdateStatus(String status, ReportListActivity.MyCallBackClass myCallBackClass) {
        this.logReportUpdateStatus(status);

        String dbStatus = status;
        if (Objects.equals(dbStatus, "SCANNER_ENLISTED")) {
            dbStatus = "NEW";
            this.setScannerEnlisted(true);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("status", dbStatus);
        reportMap.put("scannerEnlisted", scannerEnlisted);
        reportsRef.updateChildren(reportMap);
        if (myCallBackClass != null)
            myCallBackClass.execute();
    }


    public void reportUpdateExtraPhoneNumber() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        reportMap.put("extraPhoneNumber", this.extraPhoneNumber);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateIncrementalReportId() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id).child("incrementalReportId");
        reportsRef.setValue(this.incrementalReportId);
    }

    public void reportUpdateClosingText(String closingText) {
        reportUpdateCancellationText(closingText);
    }

    public void reportUpdateCancellationText(String cancellationText) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        setCancellationText(cancellationText);
        reportMap.put("cancellationText", cancellationText);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateCancellationManagerType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        setCancellationUserType("מנהל");
        reportMap.put("cancellationUserType", "מנהל");
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateAssignedScanner(String userId) {
        this.setAssignedScanner(userId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        reportMap.put("assignedScanner", userId);
        reportsRef.updateChildren(reportMap);

    }

    /**
     * @param userId
     */
    public void reportUpdateManagerInCharge(String userId) {
        this.setManagerInCharge(userId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("managerInCharge", userId);
        reportsRef.updateChildren(reportMap);
    }

    public void updateOnTheWayTimestamp() {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
                .format(new java.util.Date());

        this.setScannerOnTheWay(timeStamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        reportMap.put("scannerOnTheWay", timeStamp);
        reportsRef.updateChildren(reportMap);

    }

    public boolean isOpenReport() {
        if ((Objects.equals(this.getStatus(), "CANCELED")) || (Objects.equals(this.getStatus(), "CLOSED")))
            return false;
        else return true;
    }

    public String statusInHebrew(Context context) {
        return translateStatus(this.getStatus(), context);
    }

    public String translateStatus(String status, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean manager = prefs.getBoolean(context.getString(R.string.isManager), false);
        String id = prefs.getString(context.getString(R.string.UserId), "");
        if (manager) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "קיים סורק זמין");
            map.put("MANAGER_ENLISTED", "בטיפול מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "נבחר סורק לקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            return map.get(status);
        } else {
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "סורק אחר נבחר לקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            if (id.equals(assignedScanner)) {
                map.put("MANAGER_ASSIGNED_SCANNER", "צא ליעד, דווח יציאה לדרך");
            }
            return map.get(status);
        }

    }

    public String validate() {
        String error = "";
        if (reporterName.equals("")) {
            error = error + "חסר שם ";
        }
        if (phoneNumber.equals("")) {
            error = error + "חסר מספר טלפון ";
        } else {
            if (!phoneNumber.matches("^([0-9]{10})|([0-9]{3}-[0-9]{7})|([0-9]{2}-[0-9]{7})$")) {
                error = error + "מספר טלפון לא תקין";
            }
        }
        if (address.equals("")) {
            error = error + "חסרה כתובת ";
        }
        return error;
    }

    public boolean isHasSimilarReports() {
        return this.hasSimilarReports;
    }


    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", reportyName='" + reporterName + '\'' +
                ", status='" + getStatus() + '\'' +
                ", startTime='" + startTime + '\'' +
                ", address='" + address + '\'' +
                ", freeText='" + freeText + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", extraPhoneNumber='" + extraPhoneNumber + '\'' +
                ", assignedScanner='" + assignedScanner + '\'' +
                ", availableScanners=" + availableScanners +
                '}';
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getDistanceStr() {
        return "5";
    }

    public String getAvailableScannersStr() {
        return String.valueOf(availableScanners);
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }


    public void setDistancevalue(int distancevalue) {
        this.distancevalue = distancevalue;
    }

    public int getDistancevalue() {
        return distancevalue;
    }

    public void setAssignedScanner(String assignedScanner) {
        this.assignedScanner = assignedScanner;
    }

    public String getScannerOnTheWay() {
        return scannerOnTheWay;
    }

    public void setScannerOnTheWay(String scannerOnTheWay) {
        this.scannerOnTheWay = scannerOnTheWay;
    }


    private static long lastReportStartTime;

    public static void setLastReportStartTime(long lastReportStartTime) {
        Report.lastReportStartTime = lastReportStartTime;
    }

    public static long getLastReportStartTime() {

        return lastReportStartTime;
    }

    public boolean isAssignedScanner(String userId) {
        return Objects.equals(getAssignedScanner(), userId);
    }

    public String getCancellationUserType() {
        return cancellationUserType;
    }

    public void setCancellationUserType(String cancellationUserType) {
        this.cancellationUserType = cancellationUserType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getManagerInCharge() {
        return managerInCharge;
    }

    public void setManagerInCharge(String managerInCharge) {
        this.managerInCharge = managerInCharge;
    }

    public void setScannerEnlisted(boolean scannerEnlisted) {
        this.scannerEnlisted = scannerEnlisted;
    }

    public boolean isScannerEnlisted() {
        return scannerEnlisted;
    }
}
