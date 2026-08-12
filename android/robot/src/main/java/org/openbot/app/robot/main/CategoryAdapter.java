package org.openbot.app.robot.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.ItemCategoryBinding;
import org.openbot.app.robot.model.Category;
import org.openbot.app.robot.model.SubCategory;
import org.openbot.app.robot.utils.MarginItemDecoration;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

  private final List<Category> mValues;
  private OnItemClickListener<SubCategory> itemClickListener;

  public CategoryAdapter(List<Category> items, OnItemClickListener<SubCategory> itemClickListener) {
    mValues = items;
    this.itemClickListener = itemClickListener;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.title.setText(mValues.get(position).getDisplayTitle(holder.itemView.getContext()));
    holder.subCategoryList.setLayoutManager(
        new LinearLayoutManager(holder.itemView.getContext(), RecyclerView.HORIZONTAL, false));
    holder.subCategoryList.setAdapter(
        new SubCategoryAdapter(holder.mItem.getSubCategories(), itemClickListener));
    if (holder.subCategoryList.getItemDecorationCount() == 0)
      holder.subCategoryList.addItemDecoration(
          new MarginItemDecoration(
              (int)
                  holder.itemView.getContext().getResources().getDimension(R.dimen.feed_padding)));
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final RecyclerView subCategoryList;
    public Category mItem;

    public ViewHolder(ItemCategoryBinding binding) {
      super(binding.getRoot());

      title = binding.title;
      subCategoryList = binding.list;
    }
  }
}
