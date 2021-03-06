package com.exter.eveindcalc.group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.blueprint.BlueprintHistoryDA;
import com.exter.eveindcalc.manufacturing.BlueprintListActivity;
import com.exter.eveindcalc.refine.RefineListActivity;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.RefiningTask;
import exter.eveindustry.task.TaskFactory;


public class AddTaskDialogFragment extends DialogFragment
{

  static private final int REQUEST_MANUFATURING = 0;
  static private final int REQUEST_REFINING = 1;

  private class ManufacturingListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      startActivityForResult(new Intent(getActivity(), BlueprintListActivity.class),REQUEST_MANUFATURING);
    }
  }

  private class RefineListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      startActivityForResult(new Intent(getActivity(), RefineListActivity.class),REQUEST_REFINING);
    }
  }


  private class ReactionListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      ReactionTask task = factory.newReaction(16213);
      group_task.addTask("New Reaction Starbase",task);
      activity.notifyTaskChanged();
      activity.onProfitChanged();
      dismiss();
    }
  }

  private class PlanetListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      PlanetTask task = factory.newPlanet(11);
      group_task.addTask("New Planet",task);
      activity.notifyTaskChanged();
      activity.onProfitChanged();
      dismiss();
    }
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode)
    {
      case REQUEST_MANUFATURING:
        if(resultCode == Activity.RESULT_OK)
        {
          GroupTask group = (GroupTask)activity.getCurrentTask();
          ManufacturingTask task = factory.newManufacturing(data.getIntExtra("product", -1));
          SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
          task.setHardwiring(ManufacturingTask.Hardwiring.fromInt(sp.getInt("manufacturing.hardwiring", ManufacturingTask.Hardwiring.None.value)));
          task.setSolarSystem(sp.getInt("manufacturing.system", 30000142));
          BlueprintHistoryDA.Entry histent = database.da_blueprinthistory.getEntry(task.getBlueprint().product.item.id);
          if(histent != null)
          {
            task.setME(histent.getME());
            task.setTE(histent.getTE());
          }

          group.addTask(task.getBlueprint().product.item.name, task);
          activity.notifyTaskChanged();
          activity.onProfitChanged();
          dismiss();
        }
        break;
      case REQUEST_REFINING:
        if(resultCode == Activity.RESULT_OK)
        {
          GroupTask group = (GroupTask)activity.getCurrentTask();
          RefiningTask task = factory.newRefining(data.getIntExtra("refine", -1));
          group.addTask(task.getRefinable().item.item.name, task);
          activity.notifyTaskChanged();
          activity.onProfitChanged();
          dismiss();
        }
        break;
    }
  }
  
  private EICFragmentActivity activity;
  private TaskFactory factory;
  private EveDatabase database;

  @NonNull
  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity) getActivity();
    factory = ((EICApplication)activity.getApplication()).factory;
    database = ((EICApplication)activity.getApplication()).database;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.add_task, null);
    builder.setView(view);
    builder.setTitle("Add Task");

    Button bt_manufacturing = (Button) view.findViewById(R.id.bt_menu_manufacturing);
    Button bt_refine = (Button) view.findViewById(R.id.bt_menu_refore);
    Button bt_reaction = (Button) view.findViewById(R.id.bt_menu_reaction);
    Button bt_planet = (Button) view.findViewById(R.id.bt_menu_planet);

    bt_manufacturing.setOnClickListener(new ManufacturingListener());
    bt_refine.setOnClickListener(new RefineListener());
    bt_reaction.setOnClickListener(new ReactionListener());
    bt_planet.setOnClickListener(new PlanetListener());

    return builder.create();
  }
}
