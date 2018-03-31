package org.thoughtcrime.securesms.util;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.components.camera.CameraView;
import org.thoughtcrime.securesms.components.camera.QuickAttachmentDrawer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MotionEvent.class, CameraView.class, QuickAttachmentDrawer.class})
public class DoubleTapGestureDetectorUnitTest {

    private QuickAttachmentDrawer mockQuickAttachmentDrawer;
    private DoubleTapGestureDetector testDoubleTapper;
    private ImageButton mockFrontImageButton;
    private ImageButton mockBackImageButton;
    private ImageButton mockImageButton;
    private CameraView mockCameraView;
    private MotionEvent motionEvent;

    @Before
    public void setUp() {

        mockCameraView = PowerMockito.mock(CameraView.class);
        mockQuickAttachmentDrawer = PowerMockito.mock(QuickAttachmentDrawer.class);
        testDoubleTapper = new DoubleTapGestureDetector(mockQuickAttachmentDrawer, mockCameraView);
        motionEvent = PowerMockito.mock(MotionEvent.class);
        mockImageButton = PowerMockito.mock(ImageButton.class);
        mockBackImageButton = PowerMockito.mock(ImageButton.class);
        mockFrontImageButton = PowerMockito.mock(ImageButton.class);
    }

    @Test
    public void testOnDoubleTap() {

        PowerMockito.mockStatic(MotionEvent.class);

        // Stubbing values needed within th eonDoubleTap() method.
        when(MotionEvent.obtain(200, 300, 0, 0, 0, 0)).thenReturn(motionEvent);
        when(mockCameraView.isRearCamera()).thenReturn(true);
        when(mockQuickAttachmentDrawer.getSwapCameraButton()).thenReturn(mockImageButton);
        when(mockQuickAttachmentDrawer.getBackCameraIcon()).thenReturn(mockImageButton);
        when(mockQuickAttachmentDrawer.getFrontCameraIcon()).thenReturn(mockImageButton);

        // Calling the methods used in the stubs
        motionEvent = MotionEvent.obtain(200, 300, 0, 0, 0, 0);
        testDoubleTapper.onDoubleTap(motionEvent);

        // Verifying that the method flipCamera() was called by the CameraView when the onDoubleTap() method is triggered.
        verify(mockCameraView).flipCamera();
    }

    @Test
    public void testOnDoubleTapEvent() {

        PowerMockito.mockStatic(MotionEvent.class);

        // Stubbing values
        when(MotionEvent.obtain(200, 300, 0, 0, 0, 0)).thenReturn(motionEvent);

        // Calling the methods used in the stubs
        motionEvent = MotionEvent.obtain(200, 300, 0, 0, 0, 0);

        // Asserting that the event is in fact a double and not a single tap.
        assertTrue(testDoubleTapper.onDoubleTapEvent(motionEvent));
        assertFalse(testDoubleTapper.onSingleTapConfirmed(motionEvent));
    }

    @Test
    public void testCameraIconVisibility() {

        PowerMockito.mockStatic(MotionEvent.class);
        PowerMockito.mockStatic(ImageButton.class);
        PowerMockito.mockStatic(View.class);

        // Defining variables
        int VISIBLE = 0x00000000;
        int GONE = 0x00000008;

        // Stubbing values needed for the onDoubleTap() event and setVisibility()
        when(mockQuickAttachmentDrawer.getSwapCameraButton()).thenReturn(mockImageButton);
        when(mockQuickAttachmentDrawer.getBackCameraIcon()).thenReturn(mockBackImageButton);
        when(mockQuickAttachmentDrawer.getFrontCameraIcon()).thenReturn(mockFrontImageButton);
        when(MotionEvent.obtain(200, 300, 0, 0, 0, 0)).thenReturn(motionEvent);

        // Asserting that current camera view based on the set visibility for front and back camera icons.
        PowerMockito.doAnswer((Answer) invocation -> {
            assertTrue(mockCameraView.isRearCamera());
            return null;
        }).when(mockBackImageButton).setVisibility(VISIBLE);

        PowerMockito.doAnswer((Answer) invocation -> {
            assertFalse(mockCameraView.isRearCamera());
            return null;
        }).when(mockBackImageButton).setVisibility(GONE);

        PowerMockito.doAnswer((Answer) invocation -> {
            assertFalse(mockCameraView.isRearCamera());
            return null;
        }).when(mockFrontImageButton).setVisibility(VISIBLE);

        PowerMockito.doAnswer((Answer) invocation -> {
            assertTrue(mockCameraView.isRearCamera());
            return null;
        }).when(mockFrontImageButton).setVisibility(GONE);

        // Obtaining the event for double tap
        motionEvent = MotionEvent.obtain(200, 300, 0, 0, 0, 0);

        // Scenario 1:
        // Triggering the onDoubleTap() event in the rear camera view
        when(mockCameraView.isRearCamera()).thenReturn(true);
        testDoubleTapper.onDoubleTap(motionEvent);

        // Scenario 2:
        // Triggering the onDoubleTap() event in the front camera view
        when(mockCameraView.isRearCamera()).thenReturn(false);
        testDoubleTapper.onDoubleTap(motionEvent);
    }
}
