//
//  SpeakerTableViewCell.swift
//  ios
//
//  Created by Sahil Ishar on 3/15/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit
import Kingfisher

class SpeakerTableViewCell: UITableViewCell {

    @IBOutlet weak var nameLabel : UILabel!
    @IBOutlet weak var infoLabel : UILabel!
    @IBOutlet weak var speakerImage: UIImageView!
    
    func loadInfo(name: String, info: String, imgUrl: String) {
        nameLabel.text = name
        infoLabel.attributedText = formatHTMLString(info)
        speakerImage.kf_setImageWithURL(NSURL(string: imgUrl)!)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()

    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }

    func formatHTMLString(htmlString: String) -> NSAttributedString {
        return try! NSAttributedString(data: htmlString.dataUsingEncoding(NSUTF8StringEncoding, allowLossyConversion: true)!, options: [NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType], documentAttributes: nil);
    }
}
