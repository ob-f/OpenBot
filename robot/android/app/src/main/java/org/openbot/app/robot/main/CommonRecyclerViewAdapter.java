package org.openbot.app.robot.main;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter {

  protected Context mContext;
  private List<T> mDataList;
  private LayoutInflater mInflater;
  private SparseArray<int[]> mResLayoutAndViewIds;
  private OnItemClickListener mClickListener;
  private OnItemLongClickListener mLongClickListener;

  public CommonRecyclerViewAdapter(
      @NonNull Context context,
      @NonNull List<T> dataList,
      @NonNull SparseArray<int[]> resLayoutAndViewIds) {
    this.mContext = context;
    this.mInflater = LayoutInflater.from(context);
    this.mDataList = dataList;
    this.mResLayoutAndViewIds = resLayoutAndViewIds;

    checkResLayoutAndViewIds(resLayoutAndViewIds);
  }

  private void checkResLayoutAndViewIds(@NonNull SparseArray<int[]> resLayoutAndViewIds) {
    for (int i = 0; i < resLayoutAndViewIds.size(); i++) {
      int reslayout = resLayoutAndViewIds.keyAt(i);
      int[] viewIds = resLayoutAndViewIds.get(reslayout);
      View itemView = mInflater.inflate(reslayout, null);
      for (int viewId : viewIds) {
        View view = itemView.findViewById(viewId);
        if (view == null) {
          throw new IllegalStateException("Some viewIds don't be found in corresponding resLayout");
        }
      }
    }
  }

  public void setOnItemClickListener(@NonNull OnItemClickListener onItemClickListener) {
    this.mClickListener = onItemClickListener;
  }

  public void setOnItemLongClickListener(@NonNull OnItemLongClickListener onItemLongClickListener) {
    this.mLongClickListener = onItemLongClickListener;
  }

  @Override
  public int getItemCount() {
    return mDataList.size();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View convertView = mInflater.inflate(viewType, parent, false);
    return new MyViewHolder(convertView, mResLayoutAndViewIds.get(viewType));
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
    MyViewHolder mHolder = (MyViewHolder) holder;
    if (mClickListener != null) {
      mHolder.itemView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              mClickListener.onItemClick(view, position);
            }
          });
    }
    if (mLongClickListener != null) {
      mHolder.itemView.setLongClickable(true);
      mHolder.itemView.setOnLongClickListener(
          new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
              mLongClickListener.onItemLongClick(view, position);
              return true;
            }
          });
    }
    bindDataToItem(mHolder, mDataList.get(position), position);
  }

  @Override
  public int getItemViewType(int position) {
    int type = getItemResLayoutType(position);
    if (mResLayoutAndViewIds.indexOfKey(type) < 0) {
      throw new IllegalStateException(
          "the ResLayoutAndViewIds doesn't contain " + type + " item layout type");
    }
    return type;
  }

  /**
   * @param position the position of item
   * @return item layout type, it must be one key of mResLayoutAndViewIds
   */
  public abstract int getItemResLayoutType(int position);

  /** bind data to item */
  public abstract void bindDataToItem(MyViewHolder holder, T data, int position);

  public static class MyViewHolder extends RecyclerView.ViewHolder {
    public SparseArray<View> mViews;

    public MyViewHolder(@NonNull View itemView, @NonNull int[] resViewIds) {
      super(itemView);
      mViews = new SparseArray<>();
      for (int viewId : resViewIds) {
        View view = itemView.findViewById(viewId);
        mViews.put(viewId, view);
      }
    }
  }

  public interface OnItemClickListener {
    void onItemClick(View itemView, int position);
  }

  public interface OnItemLongClickListener {
    void onItemLongClick(View itemView, int position);
  }
}
