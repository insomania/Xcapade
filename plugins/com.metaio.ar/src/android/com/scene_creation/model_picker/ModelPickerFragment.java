package com.metaio.Template.scene_creation.model_picker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.superman.capade.R;
import com.metaio.sdk.MetaioDebug;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnModelPickerListener} interface
 * to handle interaction events.
 * Use the {@link ModelPickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModelPickerFragment extends DialogFragment
{
    final static public String TAG = "ModelPickerFragment";

    private OnModelPickerListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentInAppPurchase.
     */
    // TODO: Rename and change types and number of parameters
    public static ModelPickerFragment newInstance()
    {
        ModelPickerFragment fragment = new ModelPickerFragment();
        return fragment;
    }

    public ModelPickerFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initListener();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_model_picker, container, false);

        // populate the model list
        UIModelList modelList = mListener.getModelList();
        ListView listView = (ListView)rootView.findViewById(R.id.listView);
        ListViewAdapter customAdapter = new ListViewAdapter(getActivity().getApplicationContext(), R.layout.model_picker_list_row, modelList);
        listView.setAdapter(customAdapter);

        // set on item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                selectModel(position);
            }
        });

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void initListener()
    {
        try
        {
            mListener = (OnModelPickerListener) getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnModelPickerListener");
        }
    }

    private void selectModel(int id)
    {
        MetaioDebug.log(TAG + "[selectModel]");
        if (mListener!=null)
        {
            mListener.selectModel(id);
        }
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
    public interface OnModelPickerListener
    {
        void selectModel(int modelNumber);
        UIModelList getModelList();
    }
}
