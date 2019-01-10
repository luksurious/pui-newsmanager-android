package es.upm.hcid.newsmanager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ImageSourceListDialogFragment extends BottomSheetDialogFragment {
    /**
     * Listener which is notified of the result
     */
    private Listener mListener;

    @NonNull
    public static ImageSourceListDialogFragment newInstance() {
        return new ImageSourceListDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_imagesource_list_dialog, container, false);

        // get the views and attach the click listeners
        TextView addGalleryText = (TextView) view.findViewById(R.id.tv_btn_add_photo_gallery);
        addGalleryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onGalleryPhotoClicked();
                    dismiss();
                }
            }
        });

        TextView addCameraText = (TextView) view.findViewById(R.id.tv_btn_add_photo_camera);
        addCameraText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCameraClicked();
                    dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    /**
     * Interface for this sheet, must be implemented to get notifications of the clicks
     */
    public interface Listener {
        void onGalleryPhotoClicked();
        void onCameraClicked();
    }
}
