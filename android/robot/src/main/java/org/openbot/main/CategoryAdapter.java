package org.openbot.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.openbot.R;
import org.openbot.databinding.ItemCategoryBinding;
import org.openbot.model.Category;
import org.openbot.model.SubCategory;
import org.openbot.utils.MarginItemDecoration;

import java.util.List;

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
    // Fetch the title using the getTitle() method with context
    holder.title.setText(mValues.get(position).getTitle(holder.itemView.getContext()));
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
