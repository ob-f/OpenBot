package org.openbot.app.robot.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.databinding.ItemSubCategoryBinding;
import org.openbot.app.robot.model.SubCategory;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {

  private final List<SubCategory> mValues;
  private final OnItemClickListener<SubCategory> itemClickListener;

  public SubCategoryAdapter(
      List<SubCategory> items, OnItemClickListener<SubCategory> itemClickListener) {
    mValues = items;
    this.itemClickListener = itemClickListener;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        ItemSubCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
        mValues,
        itemClickListener);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    SubCategory item = mValues.get(position);
    holder.title.setText(item.getDisplayTitle(holder.itemView.getContext()));
    Glide.with(holder.itemView).load(item.getImage()).centerInside().into(holder.icon);
    int color = Color.parseColor(item.getBackgroundColor());
    //        Color.rgb(holder.rand.nextInt(255), holder.rand.nextInt(255),
    // holder.rand.nextInt(255));
    holder.icon.setStrokeColor(ColorStateList.valueOf(color));
    holder.icon.setBackgroundColor(color);
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final ShapeableImageView icon;
    //    public Random rand = new Random();

    public ViewHolder(
        ItemSubCategoryBinding binding,
        List<SubCategory> mValues,
        OnItemClickListener<SubCategory> itemClickListener) {
      super(binding.getRoot());
      title = binding.title;
      icon = binding.image;
      binding
          .getRoot()
          .setOnClickListener(
              v -> itemClickListener.onItemClick(mValues.get(getAdapterPosition())));
    }
  }
}
