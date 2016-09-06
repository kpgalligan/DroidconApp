//
//  SponsorsCollectionViewController.swift
//  
//
//  Created by Wojciech Dziemianczyk on 8/30/16.
//
//

import UIKit

private let reuseIdentifier = "Cell"

class SponsorsCollectionViewController: UICollectionViewController {

    // MARK: - Variables
    private let reuseIdentifier = "FlickrCell"
    private let sectionInsets = UIEdgeInsets(top: 50.0, left: 20.0, bottom: 50.0, right: 20.0)
    
    private var searches = [SponsorResults]()
    private let sponsorApi = SponsorImage()
    
}

extension SponsorsCollectionViewController : UICollectionViewDataSource {
    
    override func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
        return searches.count
    }
    
    override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return searches[section].searchResults.count
    }
    
    override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier(reuseIdentifier, forIndexPath: indexPath) as! UICollectionViewCell
        cell.backgroundColor = UIColor.blackColor()
        cell.imageView.image = flickrPhoto.thumbnail
        // Configure the cell
        return cell
    }
    
    func photoForIndexPath(indexPath: NSIndexPath) -> SponsorItem {
        return searches[indexPath.section].searchResults[indexPath.row]
    }
}

//extension FlickrPhotosViewController : UICollectionViewDelegateFlowLayout {
//    //1
//    func collectionView(collectionView: UICollectionView,
//                        layout collectionViewLayout: UICollectionViewLayout,
//                               sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
//        
//        let flickrPhoto =  photoForIndexPath(indexPath)
//        //2 Determine size of cell according to the size of the thumbnail.
//        if var size = flickrPhoto.thumbnail?.size {
//            size.width += 10
//            size.height += 10
//            return size
//        }
//        return CGSize(width: 100, height: 100)
//    }
//    
//    //3 Returns a constant for spacing between the cells, headers, and footers.
//    func collectionView(collectionView: UICollectionView,
//                        layout collectionViewLayout: UICollectionViewLayout,
//                               insetForSectionAtIndex section: Int) -> UIEdgeInsets {
//        return sectionInsets
//    }
//}
