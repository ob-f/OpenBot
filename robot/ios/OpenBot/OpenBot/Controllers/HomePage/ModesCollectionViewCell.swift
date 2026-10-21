//
// Created by Sparsh Jain on 18/08/22.
//

import UIKit

class modesCollectionViewCell: UICollectionViewCell {
    var icon: UIImageView!
    var title: UITextView!
    var color: UIColor!
    static var identifier = "modesCollectionViewCell"
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        icon = UIImageView(frame: CGRect(x: 0, y: frame.size.height/6, width: frame.size.width, height: frame.size.height/1.5))
        icon.contentMode = .scaleAspectFit
        contentView.addSubview(icon)
        
        title = UITextView(frame: CGRect(x: 0, y: frame.size.height, width: frame.size.width, height: 50))
        title.isEditable = false
        title.isScrollEnabled = false
        title.textAlignment = .left
        title.font = UIFont.systemFont(ofSize: 12)
        
        contentView.addSubview(title)
        
        // Set background color of cell
        self.contentView.backgroundColor = color
        self.contentView.layer.cornerRadius = 10
    }
        
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

    override func awakeFromNib() {
        super.awakeFromNib()
    }

    public func configure(with gridItem: ModeItem) {
        icon.image = gridItem.icon;
        title.text = gridItem.label;
        color = gridItem.color;
        self.contentView.backgroundColor = color
    }

    static func nib() -> UINib {
        UINib(nibName: "modesCollectionViewCell", bundle: nil);
    }
}
