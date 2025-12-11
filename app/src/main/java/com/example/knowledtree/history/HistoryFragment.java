package com.example.knowledtree.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Lớp API thực tế của bạn
import com.example.knowledtree.api;
import com.example.knowledtree.ParkingRecord;
import com.example.knowledtree.R;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<ParkingRecord> recordList = new ArrayList<>();
    private ProgressBar progressBar;

    // Khai báo biến service sử dụng giao diện thực tế của bạn
    private api.ApiInterface apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.history_recycler_view);
        progressBar = view.findViewById(R.id.history_progress_bar);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // *LƯU Ý: Đảm bảo lớp HistoryAdapter.java đã được tạo*
        adapter = new HistoryAdapter(recordList);
        recyclerView.setAdapter(adapter);

        // Khởi tạo service ở đây (hoặc trong onCreate/onViewCreated)
        apiService = api.getApi();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchHistoryRecords(); // Bắt đầu gọi API
    }

    private void fetchHistoryRecords() {
        progressBar.setVisibility(View.VISIBLE); // Hiển thị loading

        // 1. Lấy Service (Không cần tạo lại, đã khởi tạo ở onCreateView)
        // apiService đã được gán = api.getApi();

        // 2. Gọi API /history (Truyền null, null để lấy tất cả)
        Call<List<ParkingRecord>> call = apiService.getHistory(null, null); // ❗ Dùng apiService đã sửa

        call.enqueue(new Callback<List<ParkingRecord>>() {
            @Override
            public void onResponse(Call<List<ParkingRecord>> call, Response<List<ParkingRecord>> response) {
                progressBar.setVisibility(View.GONE); // Ẩn loading

                if (response.isSuccessful() && response.body() != null) {
                    List<ParkingRecord> records = response.body();

                    if (records.isEmpty()) {
                        Toast.makeText(getContext(), "Không có lịch sử đỗ xe.", Toast.LENGTH_SHORT).show();
                    }

                    // *LƯU Ý: Đảm bảo HistoryAdapter có phương thức updateData*
                    adapter.updateData(records);

                } else {
                    Log.e(TAG, "Lỗi phản hồi: Code " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ParkingRecord>> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Ẩn loading
                Log.e(TAG, "Lỗi kết nối mạng/Server: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi mạng hoặc server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}