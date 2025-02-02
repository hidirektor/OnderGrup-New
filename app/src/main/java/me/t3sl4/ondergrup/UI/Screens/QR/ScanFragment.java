package me.t3sl4.ondergrup.UI.Screens.QR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.t3sl4.ondergrup.R;
import me.t3sl4.ondergrup.Util.QR.QRAnalyzer;
import me.t3sl4.ondergrup.Util.QR.QRUtil;
import me.t3sl4.ondergrup.databinding.FragmentScanBinding;


/** @noinspection ALL*/
public class ScanFragment extends Fragment{

    private FragmentScanBinding binding;
    String permission = Manifest.permission.CAMERA;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private boolean isFlashOn;
    Preview preview = new Preview.Builder().build();
    CameraSelector cameraSelector;
    QRAnalyzer analyzer;
    Camera camera;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // requireActivity().getWindow().setFlags(1024, 1024);

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Start Scanning.
                        startScanning();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied.
                        QRUtil.cameraPermissionRequest(getContext(),
                                () -> QRUtil.openPermissionSettings(requireContext()));
                        startScanning();
                    }
                });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK)
                        {
                            Intent data = result.getData();

                            if (data != null)
                            {
                                Uri imageUri = data.getData();

                                if (imageUri != null)
                                {
                                    performScanning(imageUri);
                                }
                            }
                        }
                    }
                });

        requestCameraAndStartScanner();

        analyzer = new QRAnalyzer(requireActivity().getSupportFragmentManager());

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentScanBinding.inflate(inflater, container, false);

        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);

        binding.bar.setAnimation(animation);

        binding.btnGallery.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            galleryLauncher.launch(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());
        super.onResume();
    }

    private void requestCameraAndStartScanner() {
        if (QRUtil.isPermissionGranted(getContext(), permission))
        {
            // start scanner
            startScanning();
        } else {

            requestCameraPermission();
        }
    }

    private void startScanning() {

        cameraExecutor = Executors.newSingleThreadExecutor();

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider =
                        (ProcessCameraProvider) cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraPreview(ProcessCameraProvider cameraProvider) {

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());

        cameraProvider.unbindAll();
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

        binding.btnFlash.setOnClickListener(v -> {
            navigateFlash();
        });
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(permission)) {
            QRUtil.cameraPermissionRequest(getContext(),
                    () -> QRUtil.openPermissionSettings(requireContext()));
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void performScanning(Uri imageUri) {

        // Load the selected image
        Bitmap bitmap = loadBitmapFromUri(imageUri);

        if (bitmap != null) {
            analyzer.analyzeBitmap(bitmap);
        }
    }

    private Bitmap loadBitmapFromUri(Uri imageUri) {

        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);

            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e){

            e.printStackTrace();

            return null;
        }
    }

    private void navigateFlash() {
        try {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                if (isFlashOn) {
                    camera.getCameraControl().enableTorch(false);
                    binding.btnFlash.setImageResource(R.drawable.flash_off);
                    isFlashOn = false;
                } else {
                    camera.getCameraControl().enableTorch(true);
                    binding.btnFlash.setImageResource(R.drawable.flash_on);
                    isFlashOn = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

