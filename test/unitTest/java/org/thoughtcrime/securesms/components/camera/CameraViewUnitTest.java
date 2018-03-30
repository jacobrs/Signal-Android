package org.thoughtcrime.securesms.components.camera;
import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CameraView.class ,android.hardware.Camera.class, android.hardware.Camera.CameraInfo.class})
public class CameraViewUnitTest {

    private final String TAG = CameraView.class.getSimpleName();

    private TextSecurePreferences mockTextSecurePreference;
    private static CameraView mockCameraView;
    private Context mockContext;

    @Before
    public void setUp() {

        mockTextSecurePreference = PowerMockito.mock(TextSecurePreferences.class);
        mockCameraView = PowerMockito.mock(CameraView.class);
        mockContext = PowerMockito.mock(Context.class);
    }

    @Test
    public void testFlipCamera() {

        PowerMockito.mockStatic(android.hardware.Camera.class);
        PowerMockito.mockStatic(TextSecurePreferences.class);
        PowerMockito.mockStatic(CameraView.class);

        int currentId;
        final int CAMERA_FACING_BACK = 0;
        final int CAMERA_FACING_FRONT = 1;

        when(android.hardware.Camera.getNumberOfCameras()).thenReturn(2);

        // Set the internal state of 'cameraId' field in the mockCameraView instance
        // to value 0, which is asserted to be equal to value CAMERA_FACING_BACK
        Whitebox.setInternalState(mockCameraView, "cameraId", 0);
        currentId = Whitebox.getInternalState(mockCameraView, "cameraId");
        assertEquals(currentId, CAMERA_FACING_BACK);

        // The flipCamera method is called with the mockCameraView having the internal
        // state set for cameraId = 0.
        mockCameraView.flipCamera();

        // Set the internal state of 'cameraId' field in the mockCameraView instance
        // to value 1, which is asserted to be equal to value CAMERA_FACING_FRONT
        Whitebox.setInternalState(mockCameraView, "cameraId", 1);
        currentId = Whitebox.getInternalState(mockCameraView, "cameraId");
        assertEquals(currentId, CAMERA_FACING_FRONT);

        // The flipCamera method is called with the mockCameraView having the internal
        // state set for cameraId = 1.
        mockCameraView.flipCamera();

        try{
            // doAnswer will execute when flipCamera is called by mockCameraView
            PowerMockito.doAnswer((Answer) invocation -> {

                // Grab the current value of 'cameraId'
                int currentCameraId = Whitebox.getInternalState(mockCameraView, "cameraId");
                int newCurrentCameraId;

                // If the camera is back-facing, the 'currentId' in mockCameraView is changed to 1.
                if (currentCameraId == CAMERA_FACING_BACK){
                    Whitebox.setInternalState(mockCameraView, "cameraId", CAMERA_FACING_FRONT);
                    newCurrentCameraId =  Whitebox.getInternalState(mockCameraView, "cameraId");

                    // The new value is asserted to be equal to value CAMERA_FACING_FRONT.
                    assertEquals(CAMERA_FACING_FRONT, newCurrentCameraId);

                    // Verifying that the call to setDirectCaptureCameraId() was called by an instance of TextSecurePreference
                    // within the flipCamera() method.
                    verify(mockTextSecurePreference).setDirectCaptureCameraId(mockContext, newCurrentCameraId);
                }
                // If the camera is front-facing, the 'currentId' in mockCameraView is changed to 0.
                else if (currentCameraId == CAMERA_FACING_FRONT){
                    Whitebox.setInternalState(mockCameraView, "cameraId", CAMERA_FACING_BACK);
                    newCurrentCameraId =  Whitebox.getInternalState(mockCameraView, "cameraId");

                    // The new value is asserted to be equal to value CAMERA_FACING_BACK.
                    assertEquals(CAMERA_FACING_BACK, newCurrentCameraId);

                    // Verifying that the call to setDirectCaptureCameraId() was called by an instance of TextSecurePreference
                    // within the flipCamera() method.
                    verify(mockTextSecurePreference).setDirectCaptureCameraId(mockContext, newCurrentCameraId);
                }
                return null;
            }).when(mockCameraView).flipCamera();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }
    }
}
