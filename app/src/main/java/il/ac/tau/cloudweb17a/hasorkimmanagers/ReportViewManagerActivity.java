package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ReportViewManagerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 15;

    private Report report;
    TextView managerReportCurrentScanner;

    final String TAG = ReportViewManagerActivity.class.getSimpleName();

    ArrayList<Scanner> scannerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view_manager);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.manager_map);
        mapFragment.getMapAsync(this);

        report = (Report) getIntent().getSerializableExtra("Report");

        scannerList = new ArrayList<>();
        final ScannerAdapter adapter = new ScannerAdapter(this, scannerList);

        ListView listView = findViewById(R.id.list_view_scanners);
        listView.setAdapter(adapter);

        //String reportStatus = report.getStatus();

        final TextView managerReportStatus = findViewById(R.id.managerReportStatus);
        managerReportStatus.setText(report.statusInHebrew());

        TextView managerReportLocation = findViewById(R.id.managerReportLocation);
        managerReportLocation.setText(report.getAddress());

        TextView managerReportOpenTime = findViewById(R.id.managerReportOpenTime);
        managerReportOpenTime.setText(report.getStartTimeAsString());

        TextView managerReportReporterName = findViewById(R.id.managerReportReporterName);
        managerReportReporterName.setText(report.getReporterName());

        TextView managerReportPhoneNumber = findViewById(R.id.managerReportPhoneNumber);
        managerReportPhoneNumber.setText(report.getPhoneNumber());

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.managerReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.managerReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

        if (report.getImageUrl() != null) {
            ImageView managerReportImage = findViewById(R.id.managerReportImage);
            managerReportImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(report.getImageUrl()).into(managerReportImage);
        }

        DatabaseReference statusManagerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("status");

        statusManagerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                managerReportStatus.setText(report.statusInHebrew());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        scannerList = new ArrayList<>();

        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("potentialScanners");
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {

                    final String userId = messageSnapshot.getKey();
                    final String duration = messageSnapshot.getValue().toString();
                    final boolean isAssignedScanner = Objects.equals(userId, report.getAssignedScanner());

                    Query mUserReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").orderByChild("id").equalTo(userId);

                    mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                User dbUser = user.getValue(User.class);
                                adapter.add(new Scanner(userId, dbUser.getName(), duration, isAssignedScanner));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendScannerHandler(View view) {
        LinearLayout vwParentRow = (LinearLayout) view.getParent();
        TextView scanner_name = (TextView) vwParentRow.getChildAt(1);

        String scannerNameString = scanner_name.getText().toString();

        Button sendScanner = (Button) vwParentRow.getChildAt(2);

        String assignedScanner = report.getAssignedScanner();

        if (assignedScanner != null && !Objects.equals(assignedScanner, "") &&
                !Objects.equals(assignedScanner, scannerNameString)) {
            sendScanner.setError("יכול להיות רק סורק מאושר אחד");
        } else {

            if (!Objects.equals(report.getAssignedScanner(), scannerNameString)) {
                report.reportUpdateAssignedScanner(scannerNameString);
                sendScanner.setText(R.string.scanner_was_chosen);
                report.setStatus("MANAGER_ASSIGNED_SCANNER");
                report.reportUpdateStatus("MANAGER_ASSIGNED_SCANNER", null);
                view.setBackgroundColor(Color.CYAN);
            }
            /*
            else {
                report.reportUpdateAssignedScanner("");
                sendScanner.setText(R.string.choose_scanner);
                vwParentRow.setBackgroundColor(Color.WHITE);
                report.setStatus("MANAGER_ENLISTED");
                report.reportUpdateStatus("MANAGER_ENLISTED");
            }
            */
            else {
                report.reportUpdateAssignedScanner("");
                sendScanner.setText(R.string.choose_scanner);
                report.setStatus("NEW");
                report.reportUpdateStatus("NEW", null);
                view.setBackgroundColor(Color.LTGRAY);
            }
        }

        vwParentRow.refreshDrawableState();

        //Log.d(TAG, scanner_name.getText().toString());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(report.getLat(), report.getLong());
        mMap.addMarker(new MarkerOptions().position(location).title("מיקום הדיווח")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }

    /*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
        finish();
    }
    */
}









