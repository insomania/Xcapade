package com.metaio.Template.scene_playback.scene_picker;

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
 * {@link OnScenePickerListener} interface
 * to handle interaction events.
 * Use the {@link ScenePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScenePickerFragment extends DialogFragment
{
    final static public String TAG = "ScenePickerFragment";

    private OnScenePickerListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentInAppPurchase.
     */
    // TODO: Rename and change types and number of parameters
    public static ScenePickerFragment newInstance()
    {
        ScenePickerFragment fragment = new ScenePickerFragment();
        return fragment;
    }

    public ScenePickerFragment()
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
        final View rootView = inflater.inflate(R.layout.fragment_scene_picker, container, false);

        // populate the scene list
        UISceneList sceneList = mListener.getSceneList();
        ListView listView = (ListView)rootView.findViewById(R.id.listView);
        SceneListViewAdapter customAdapter = new SceneListViewAdapter(getActivity().getApplicationContext(), R.layout.scene_picker_list_row, sceneList);
        listView.setAdapter(customAdapter);

        // set on item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                selectScene(position);
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
            mListener = (OnScenePickerListener) getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnScenePickerListener");
        }
    }

    private void selectScene(int id)
    {
        MetaioDebug.log(TAG + "[loadScene]");
        if (mListener!=null)
        {
            mListener.selectScene(id);
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
    public interface OnScenePickerListener
    {
        void selectScene(int sceneNumber);
        UISceneList getSceneList();
    }
}
