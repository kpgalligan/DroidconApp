//
//  EditProfileViewController.swift
//  ios
//
//  Created by Ramona Harrison on 7/20/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit
import Toast_Swift

class EditProfileViewController: UIViewController,  UIImagePickerControllerDelegate, UINavigationControllerDelegate, DCPEditProfileHost {
    
    // MARK: Properties

    @IBOutlet weak var nameField: UITextField!
    @IBOutlet weak var phoneField: UITextField!
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var companyField: UITextField!
    @IBOutlet weak var websiteField: UITextField!
    @IBOutlet weak var facebookField: UITextField!
    @IBOutlet weak var twitterField: UITextField!
    @IBOutlet weak var gplusField: UITextField!
    @IBOutlet weak var linkedInField: UITextField!
    @IBOutlet weak var hideEmailSwitch: UISwitch!
    @IBOutlet weak var profileImageView: UIImageView!
    @IBOutlet weak var bioTextView: UITextView!
    @IBOutlet weak var phoneIcon: UIImageView!
    @IBOutlet weak var emailIcon: UIImageView!
    @IBOutlet weak var companyIcon: UIImageView!
    @IBOutlet weak var bioIcon: UIImageView!
    @IBOutlet weak var websiteIcon: UIImageView!
    @IBOutlet weak var navBar: UINavigationBar!
    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var contentView: UIView!
    
    var editProfilePresenter: DCPEditProfilePresenter!
    let imagePicker = UIImagePickerController()
    
    // MARK: Lifecycle events
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if editProfilePresenter != nil {
            editProfilePresenter.unregister()
        }
        
        editProfilePresenter = DCPEditProfilePresenter(androidContentContext: DCPAppManager.getContext(), withDCPEditProfileHost: self, withBoolean: false)
        
        imagePicker.delegate = self
        
        navBar.translucent = false
        
        profileImageView.layer.cornerRadius = 21
        profileImageView.layer.masksToBounds = true
        
        // Setup tap gesture recognizer on the profile photo
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(EditProfileViewController.profileImageTapped(_:)))
        profileImageView.addGestureRecognizer(tapGesture)
        profileImageView.userInteractionEnabled = true
        
        phoneIcon.image = phoneIcon.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        phoneIcon.tintColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        
        emailIcon.image = emailIcon.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        emailIcon.tintColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        
        companyIcon.image = companyIcon.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        companyIcon.tintColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        
        bioIcon.image = bioIcon.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        bioIcon.tintColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        
        websiteIcon.image = websiteIcon.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        websiteIcon.tintColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        
    }
    
    override func viewDidLayoutSubviews() {
        scrollView.addSubview(contentView)
        scrollView.contentSize = contentView.frame.size
    }
    
    override func viewWillDisappear(animated: Bool) {
        editProfilePresenter.unregister()
        super.viewWillDisappear(animated)
    }
    
    // MARK: Actions
    
    func profileImageTapped(gesture: UIGestureRecognizer) {
        imagePicker.allowsEditing = true
        imagePicker.sourceType = .PhotoLibrary
        
        presentViewController(imagePicker, animated: true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        if let pickedImage = info[UIImagePickerControllerOriginalImage] as? UIImage {
            profileImageView.contentMode = .ScaleAspectFill
            profileImageView.image = pickedImage
            
            let imageURL = info[UIImagePickerControllerReferenceURL] as! NSURL
            let imageName = imageURL.lastPathComponent
            let documentDirectory = NSFileManager.defaultManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask)[0]
            let localURL = documentDirectory.URLByAppendingPathComponent(imageName!)
            let path = localURL.path!
            
            let image = info[UIImagePickerControllerOriginalImage] as! UIImage
            let data = UIImagePNGRepresentation(image)
            data!.writeToFile(path, atomically: true)
            print(path)
            editProfilePresenter.uploadProfilePhotoWithNSString(path)
        }
        
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func saveProfile(sender: UIBarButtonItem) {
        let success = editProfilePresenter.saveProfileWithNSString(nameField.text,
                                                                   withNSString: bioTextView.text,
                                                                   withNSString: companyField.text,
                                                                   withNSString: twitterField.text,
                                                                   withNSString: linkedInField.text,
                                                                   withNSString: websiteField.text,
                                                                   withNSString: facebookField.text,
                                                                   withNSString: phoneField.text,
                                                                   withNSString: emailField.text,
                                                                   withNSString: gplusField.text,
                                                                   withBoolean: !hideEmailSwitch.on)
        
        if (success) {
            showMessageWithNSString("Profile updated.")
        }
    }
    
    func setUserAccountWithDCDUserAccount(ua: DCDUserAccount!) {
        setProfilePhotoWithNSString(ua.avatarImageUrl(), withNSString: ua.getName())
        nameField.text = ua.getName()
        phoneField.text = ua.getPhone()
        emailField.text = ua.getEmail()
        companyField.text = ua.getCompany()
        websiteField.text = ua.getWebsite()
        facebookField.text = ua.getFacebook()
        twitterField.text = ua.getTwitter()
        gplusField.text = ua.getgPlus()
        linkedInField.text = ua.getLinkedIn()
        bioTextView.text = ua.getProfile()
        hideEmailSwitch.on = !ua.getEmailPublic()
    }
    
    func setProfilePhotoWithNSString(avatarUrl: String!, withNSString name: String!) {
        if avatarUrl != nil {
            profileImageView.kf_setImageWithURL(NSURL(string: avatarUrl)!)
        }
    }
    
    func showMessageWithNSString(msg: String!) {
        var style = ToastStyle()
        style.backgroundColor = UIColor(red: 0/255.0, green: 65/255.0, blue: 163/255.0, alpha: 1.0)
        self.view.makeToast(msg, duration: 3.0, position: .Bottom, style: style)
    }

}
