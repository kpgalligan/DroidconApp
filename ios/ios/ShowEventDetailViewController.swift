//
//  ShowEventDetailViewController.swift
//  ios
//
//  Created by Sahil Ishar on 3/14/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

@objc class ShowEventDetailViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, DCPEventDetailHost {
    
    // MARK: Properties
    
    var titleString: String?
    var descriptionString: String?
    var dateTime: String?
    var trackNumString: String?
    var event: DCDEvent!
    var speakers: [DCDEventSpeaker]?
    var eventDetailPresenter: DCPEventDetailPresenter!
    
    @IBOutlet weak var tableView : UITableView!
    @IBOutlet weak var rsvpButton: UIButton!
    
    // MARK: Lifecycle events
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(true)
        
        let tintColor = UIColor(red:(0/255.0), green:(90/255.0), blue:(224/255.0), alpha: 1.0)
        navigationController!.navigationBar.barTintColor = tintColor
        navigationController!.navigationBar.translucent = false
        UINavigationBar.appearance().tintColor = UIColor.whiteColor()
        
        // Hide the nav bar bottom shadow
        navigationController!.navigationBar.setBackgroundImage(UIImage(), forBarPosition: UIBarPosition.Any, barMetrics: UIBarMetrics.Default)
        navigationController!.navigationBar.shadowImage = UIImage()
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if eventDetailPresenter != nil {
            eventDetailPresenter.unregister()
        }
        
        eventDetailPresenter = DCPEventDetailPresenter(androidContentContext: DCPAppManager.getContext(), withLong: event.getId(), withDCPEventDetailHost: self)
        
        let nib = UINib(nibName: "EventTableViewCell", bundle: nil)
        tableView.registerNib(nib, forCellReuseIdentifier: "eventCell")
        
        let nib2 = UINib(nibName: "SpeakerTableViewCell", bundle: nil)
        tableView.registerNib(nib2, forCellReuseIdentifier: "speakerCell")
        
        self.tableView.contentInset = UIEdgeInsetsZero
        self.tableView.separatorStyle = .None
        
        print(dateTime!)
        print(trackNumString!)
        
        self.tableView.reloadData()
        
        styleButton()
    }
    
    override func viewDidDisappear(animated: Bool) {
        eventDetailPresenter.unregister()
        //        eventDetailPresenter = nil
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: Data refresh
    
    func dataRefresh() {
        event = eventDetailPresenter.getEventDetailLoadTask().getEvent()
        speakers = PlatformContext_iOS.javaListToNSArray(eventDetailPresenter.getEventDetailLoadTask().getEvent().getSpeakerList()) as? [DCDEventSpeaker]
        tableView.reloadData()
        updateButton()
    }
    
    // MARK: TableView
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return 1
        }
        
        return speakers!.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            let cell:EventTableViewCell = tableView.dequeueReusableCellWithIdentifier("eventCell") as! EventTableViewCell
            
            cell.loadInfo(titleString!, description: descriptionString!, track: trackNumString!, time: dateTime!, event: event, eventDetailPresenter: eventDetailPresenter)
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            return cell
        } else {
            let cell:SpeakerTableViewCell = tableView.dequeueReusableCellWithIdentifier("speakerCell") as! SpeakerTableViewCell
            
            let speaker = speakers![indexPath.row] as DCDEventSpeaker
            if let speakerDescription = speakers?[indexPath.row].valueForKey("userAccount_")!.valueForKey("profile_") {
                let userAccount = speaker.getUserAccount()
                let imageUrl = userAccount!.avatarImageUrl() ?? ""
                cell.loadInfo(userAccount!.valueForKey("name_") as! String, info: speakerDescription as! String, imgUrl: imageUrl)
            }
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            return cell
        }
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == 0 {
            return nil
        }
        
        return ""
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0
        }
        
        return 0
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 {
            let attrTitle = [NSFontAttributeName: UIFont.systemFontOfSize(16.0)]
            let szTitle = CGSize(width: view.bounds.width - 16, height:200)
            let rTitle = titleString!.boundingRectWithSize(szTitle, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrTitle, context:nil)
            let htTitle = ceil(rTitle.size.height)
            
            let attrDescription = [NSFontAttributeName: UIFont.systemFontOfSize(16.0)]
            let szDescription = CGSize(width: view.bounds.width - 16, height:500)
            let rDescription = descriptionString!.boundingRectWithSize(szDescription, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrDescription, context:nil)
            let htDescription = ceil(rDescription.size.height)

            return htTitle + htDescription + 60
        }

        
        if let speakerDescription = speakers?[indexPath.row].valueForKey("userAccount_")!.valueForKey("profile_") {
            let attrDescription = [NSFontAttributeName: UIFont.systemFontOfSize(16.0)]
            let szDescription = CGSize(width: view.bounds.width - 16, height:500)
            let rDescription = speakerDescription.boundingRectWithSize(szDescription, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrDescription, context:nil)
            let htDescription = ceil(rDescription.size.height)
            
            return htDescription + 50
        }
        
        return 200
    }
    
    func tableView(tableView: UITableView, estimatedHeightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 500.0
    }
    
    // MARK: Button
    
    func styleButton() {
        rsvpButton.layer.cornerRadius = 24
        rsvpButton.layer.masksToBounds = true
        rsvpButton.layer.shadowColor = UIColor.blackColor().CGColor
        rsvpButton.layer.shadowOffset = CGSizeMake(5, 5)
        rsvpButton.layer.shadowRadius = 5
        rsvpButton.layer.shadowOpacity = 1.0
        updateButton()
    }

    
    func updateButton() {
        if (event.isRsvped()) {
            rsvpButton.setImage(UIImage(named: "ic_done"), forState: .Normal)
            rsvpButton.backgroundColor = UIColor.whiteColor()
        } else {
            rsvpButton.setImage(UIImage(named: "ic_add"), forState: .Normal)
            rsvpButton.backgroundColor = UIColor(red: 93/255.0, green: 253/255.0, blue: 173/255.0, alpha: 1.0)
        }
    }
    
    @IBAction func toggleRsvp(sender: UIButton) {
        eventDetailPresenter.toggleRsvp()
    }
}
