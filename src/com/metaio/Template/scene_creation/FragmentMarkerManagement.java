package com.metaio.Template.scene_creation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xcapade.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentMarkerManagementInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentMarkerManagement#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMarkerManagement extends Fragment
{
    public static final String TAG = "FragmentMarkerManagement";

    private OnFragmentMarkerManagementInteractionListener mListener;

    private View mMarkerEditView;
    private TextView mMarkerIDText;

    public FragmentMarkerManagement()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentMarkerManagement.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMarkerManagement newInstance()
    {
        FragmentMarkerManagement fragment = new FragmentMarkerManagement();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initListener();
    }

    private void initListener()
    {
        try
        {
            mListener = (OnFragmentMarkerManagementInteractionListener) getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentMarkerManagementInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_marker_management, container, false);

        View btn = root.findViewById(R.id.remove_marker_button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onButtonRemovePressed();
            }
        });

        btn = root.findViewById(R.id.insert_model_button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onButtonAddModelPressed();
            }
        });

        btn = root.findViewById(R.id.delete_model_button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onButtonRemoveModelPressed();
            }
        });

        mMarkerEditView = root.findViewById(R.id.marker_edit_view);

        mMarkerIDText = (TextView)root.findViewById(R.id.marker_id_text);

        return root;
    }

    public void setCOSVisibility(boolean vis, int num)
    {
        if (mMarkerEditView !=null)
        {
            mMarkerEditView.setVisibility(vis ? View.VISIBLE : View.GONE);
        }
        setMarkerNumber(num);
    }

    private void setMarkerNumber(int num)
    {
        if (mMarkerIDText!=null)
        {
            mMarkerIDText.setText("Scene " + new Integer(num).toString());
        }
    }

    private void onButtonRemovePressed()
    {
        if (mListener != null)
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Remove Marker")
                    .setMessage("Are you sure you want to delete marker and its models?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mListener.removeMarker();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    })
                    .show();
        }
    }

    private void onButtonAddModelPressed()
    {
        mListener.addModel();
    }

    private void onButtonRemoveModelPressed()
    {
        mListener.removeModel();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentMarkerManagementInteractionListener
    {
        void removeMarker();
        void addModel();
        void removeModel();
    }
}
