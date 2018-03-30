package org.thoughtcrime.securesms.util;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.components.camera.CameraView;
import org.thoughtcrime.securesms.components.camera.QuickAttachmentDrawer;

public class DoubleTapGestureDetector extends GestureDetector.SimpleOnGestureListener {
    CameraView cameraView;
    QuickAttachmentDrawer quickAttachmentDrawer;

    public DoubleTapGestureDetector(QuickAttachmentDrawer qad, CameraView cv){
        this.cameraView = cv;
        this.quickAttachmentDrawer = qad;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e){
        cameraView.flipCamera();
        quickAttachmentDrawer.getSwapCameraButton().setImageResource(cameraView.isRearCamera() ? R.drawable.quick_camera_front
                : R.drawable.quick_camera_rear);

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e){
        return true;
    }
}
