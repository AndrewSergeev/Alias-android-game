package il.co.freebie.alias;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

/**
 * Created by one 1 on 16-Sep-18.
 */

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder>{

    private List<Team> teamsList;
    private ITeamListener listener;

    public TeamAdapter(List<Team> teamsList) {
        this.teamsList = teamsList;
    }

    interface ITeamListener {
        void onTeamClicked(View view);
    }

    public void setListener(ITeamListener listener)
    {
        this.listener = listener;
    }

    public class TeamViewHolder extends RecyclerView.ViewHolder{
        TextView teamColorTextView;
        TextView teamNameTextView;


        public TeamViewHolder(View itemView) {
            super(itemView);

            teamColorTextView = itemView.findViewById(R.id.team_color_tv);
            teamNameTextView = itemView.findViewById(R.id.team_name_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                    {
                        listener.onTeamClicked(view);
                    }
                }
            });
        }
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_layout,parent,false);
        TeamViewHolder teamViewHolder = new TeamViewHolder(view);

        return teamViewHolder;
    }

    @Override
    public void onBindViewHolder(TeamViewHolder holder, int position) {
        Team team = teamsList.get(position);
        holder.teamColorTextView.getBackground().setColorFilter(team.getTeamColor(), PorterDuff.Mode.SRC_ATOP);//CHECK
        holder.teamNameTextView.setText(team.getTeamName());
    }

    @Override
    public int getItemCount() {
        return teamsList.size();
    }
}
