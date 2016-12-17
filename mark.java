import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
//Functions as the everything-class to avoid generating lots of .class files
public class mark implements DocumentListener, ActionListener, MouseListener, WindowListener, Comparable<mark> {
	public static final int MAX_SEARCH_RESULTS = 512;
	public static mark markListener = new mark();
	//file management
	public static File extractedFileDir = new File("extracted data.win marked");
	public static File backupFolder = new File("Backup");
	public static File baseStringsFile = new File(extractedFileDir, "0.txt");
	public static File latestStringsFile = new File(extractedFileDir, "strings.txt");
	public static File stringTreeFile = new File("stringtree.txt");
	public static File stringsUnusedFile = new File("strings_unused.txt");
	public static File stringsPackedFile = new File("strings_packed.txt");
	public static String[] backupFilesToCopy = new String[] {
		"file0", "file8", "file9", "undertale.ini", "system_information_962", "system_information_963"};
	public static String fileHeaderString = "Hierarchy:\n";
	public static int nextFileNum = 1;
	public static boolean needsSaving = false;
	public static boolean displaySaveSuccessMessages = false;
	//string searching
	public static char[][] searchableStrings;
	public static ArrayList<mark> searchableStringsFoundIndices = new ArrayList<mark>(MAX_SEARCH_RESULTS);
	public static ArrayList<Integer> foundStringsIndexList = new ArrayList<Integer>();
	//UI components
	public static JFrame mainWindow;
	public static JTextField stringSearchTextField = new JTextField();
	public static DefaultListModel<String> stringSearchResultsListModel = new DefaultListModel<String>();
	public static JList<String> stringSearchResultsList = new JList<String>(stringSearchResultsListModel);
	public static JScrollPane stringSearchResultsListScrollPane = new JScrollPane();
	public static JComboBox<String> lastSaveSelector = new JComboBox<String>();
	public static DefaultComboBoxModel<String> lastSaveSelectorModel = (DefaultComboBoxModel<String>)(lastSaveSelector.getModel());
	public static JButton newSaveButton = new JButton();
	public static JButton newAttemptButton = new JButton();
	public static DefaultListModel<String> allStringsListModel = new DefaultListModel<String>();
	public static JList<String> allStringsList = new JList<String>(allStringsListModel);
	public static JScrollPane allStringsListScrollPane = new JScrollPane();
	public static JButton backupButton = new JButton();
	public static JButton exportStringsButton = new JButton();
	public static JButton exportStringTreeButton = new JButton();
	//for function outputs
	public static JComboBox<String> saveAndTextParentSavePicker = new JComboBox<String>(lastSaveSelectorModel);
	public static JTextField saveAndTextTextField = new JTextField();
	public static String saveAndTextSave = "";
	public static String saveAndTextText = "";
	public static void main(String[] args) {
		setupWindow();
		loadBaseStrings();
		loadStrings();
		saveMissingStringFiles();
		displaySaveSuccessMessages = true;
		showWindow();
	}
	public static void setupWindow() {
		JPanel mainPanel = new JPanel();

		stringSearchTextField.getDocument().addDocumentListener(markListener);

		stringSearchResultsListScrollPane.setViewportView(stringSearchResultsList);
		stringSearchResultsListScrollPane.setMaximumSize(new Dimension(500, Short.MAX_VALUE));
		stringSearchResultsList.addMouseListener(markListener);

		newSaveButton.setText("New Save");
		newSaveButton.addActionListener(markListener);

		newAttemptButton.setText("New Attempt");
		newAttemptButton.addActionListener(markListener);

		allStringsList.setVisibleRowCount(22);
		allStringsListScrollPane.setViewportView(allStringsList);
		allStringsListScrollPane.setMaximumSize(new Dimension(500, Short.MAX_VALUE));
		allStringsList.addMouseListener(markListener);

		backupButton.setText("Backup");
		backupButton.addActionListener(markListener);

		exportStringsButton.setText("Save Strings");
		exportStringsButton.addActionListener(markListener);

		exportStringTreeButton.setText("Save String Tree");
		exportStringTreeButton.addActionListener(markListener);

		Insets buttonInsets = backupButton.getMargin();
		buttonInsets.left -= 4;
		buttonInsets.right -= 4;
		backupButton.setMargin(buttonInsets);
		exportStringsButton.setMargin(buttonInsets);
		exportStringTreeButton.setMargin(buttonInsets);
		newSaveButton.setMargin(buttonInsets);
		newAttemptButton.setMargin(buttonInsets);

		GroupLayout layout = new GroupLayout(mainPanel);
		mainPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addGroup(layout.createParallelGroup()
				.addComponent(stringSearchResultsListScrollPane)
				.addComponent(stringSearchTextField)
				.addComponent(allStringsListScrollPane)
				.addComponent(lastSaveSelector)
				.addGroup(layout.createSequentialGroup()
					.addComponent(backupButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(exportStringsButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(exportStringTreeButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(newSaveButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(newAttemptButton)))
			.addContainerGap()
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addContainerGap()
			.addComponent(stringSearchTextField)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addComponent(stringSearchResultsListScrollPane)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addComponent(lastSaveSelector)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(layout.createParallelGroup()
				.addComponent(backupButton)
				.addComponent(exportStringsButton)
				.addComponent(exportStringTreeButton)
				.addComponent(newSaveButton)
				.addComponent(newAttemptButton))
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addComponent(allStringsListScrollPane)
			.addContainerGap()
		);

		mainPanel.setSize(mainPanel.getPreferredSize());

		mainWindow = new JFrame("UNDERTALE String Marking");
		mainWindow.addWindowListener(markListener);
		mainWindow.add(mainPanel);
		mainWindow.setContentPane(mainPanel);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	public static void showWindow() {
		mainWindow.setVisible(true);
		mainWindow.setLocation(5, 5);
		mainWindow.toFront();
		mainWindow.pack();

		setStartingSaveAndString();
		produceSaveAndText("Pick Starting Save", true, false);
	}
	public static boolean confirm(String message, String title) {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
	public mark() {}
////////////////////////////////////////////////////////////////
////////	Extracting from strings or save names
////////////////////////////////////////////////////////////////
	public static String lettersAbbreviationAt(String s, int start) {
		int end = start;
		for (char c; (c = s.charAt(end)) >= 'A' && c <= 'Z'; end++)
			;
		return s.substring(start, end);
	}
	//expects the string to be in 4-spaces form from allStringsListModel
	//returns the abbreviation with the attempt number
	public static String extractAbbreviationFromString(String s) {
		int abbreviationIndex = s.indexOf(' ', 5) + 4;
		return s.substring(abbreviationIndex, s.indexOf(' ', abbreviationIndex));
	}
	public static String buildAbbreviation(String s, int start) {
		StringBuilder sb = new StringBuilder();
		buildAbbreviationTo(s, start, sb);
		return sb.toString();
	}
	public static void buildAbbreviationTo(String s, int start, StringBuilder sb) {
		String sUpper = s.toUpperCase();
		for (int i = start; true;) {
			for (char c; true; i++) {
				if (i >= sUpper.length())
					return;
				if ((c = sUpper.charAt(i)) >= 'A' && c <= 'Z')
					break;
			}
			sb.append(sUpper.charAt(i));
			i++;
			for (char c; true; i++) {
				if (i >= sUpper.length())
					return;
				if ((c = sUpper.charAt(i)) < 'A' || c > 'Z')
					break;
			}
		}
	}
////////////////////////////////////////////////////////////////
////////	Retrieving strings or save names
////////////////////////////////////////////////////////////////
	public static String currentSaveAbbreviation() {
		String currentSave = (String)(lastSaveSelectorModel.getSelectedItem());
		return currentSave.substring(currentSave.indexOf('-') + 1, currentSave.indexOf(':'));
	}
	public static String currentSaveName() {
		String currentSave = (String)(lastSaveSelectorModel.getSelectedItem());
		return currentSave.substring(currentSave.indexOf(':') + 2);
	}
	//assumes (includeSave || includeText) is true
	public static boolean produceSaveAndText(String title, boolean includeSave, boolean includeText) {
		JComponent toShow;
		if (!includeText)
			toShow = saveAndTextParentSavePicker;
		else {
			if (!includeSave)
				toShow = saveAndTextTextField;
			else {
				toShow = new JPanel();
				toShow.setLayout(new BoxLayout(toShow, BoxLayout.Y_AXIS));
				toShow.add(saveAndTextParentSavePicker);
				toShow.add(saveAndTextTextField);
			}
		}
		if (JOptionPane.showConfirmDialog(
				null,
				toShow,
				title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			if (includeSave)
				saveAndTextSave = (String)(lastSaveSelectorModel.getSelectedItem());
			if (includeText)
				saveAndTextText = saveAndTextTextField.getText();
			return true;
		}
		return false;
	}
////////////////////////////////////////////////////////////////
////////	Finding strings or save names
////////////////////////////////////////////////////////////////
	//finds the hierarchy index of a save that starts with the given abbreviation after the dash
	//abbreviation must include a colon
	public static int findAbbreviation(String abbreviation) {
		int saveCount = lastSaveSelector.getItemCount();
		for (int i = 0; i < saveCount; i++) {
			String save = lastSaveSelector.getItemAt(i);
			if (save.startsWith(abbreviation, save.indexOf('-') + 1))
				return i;
		}
		return -1;
	}
	//returns 1 if the abbreviation is not found
	public static int nextAvailableAbbreviationNum(String abbreviation) {
		int saveNum = 0;
		int saveCount = lastSaveSelector.getItemCount();
		for (int i = 0; i < saveCount; i++) {
			String save = lastSaveSelector.getItemAt(i);
			int abbreviationIndex = save.indexOf('-') + 1;
			if (save.startsWith(abbreviation, abbreviationIndex)) {
				int abbreviationEndIndex = abbreviationIndex + abbreviation.length();
				char c = save.charAt(abbreviationEndIndex);
				if (c >= '0' && c <= '9')
					saveNum = Math.max(saveNum, Integer.parseInt(
						save.substring(abbreviationEndIndex, save.indexOf(':', abbreviationEndIndex + 1))));
			}
		}
		return saveNum + 1;
	}
	//childIndex is the hierarchy index of the theoretical first child
	//returns the first hierarchy index >= childIndex with a dash at or before dashIndex
	public static int skipChildren(int childIndex, int dashIndex) {
		while (childIndex < lastSaveSelector.getItemCount() &&
			lastSaveSelector.getItemAt(childIndex).indexOf('-') > dashIndex)
			childIndex++;
		return childIndex;
	}
////////////////////////////////////////////////////////////////
////////	Altering string views
////////////////////////////////////////////////////////////////
	public static void refreshStringSearchResults() {
		String fullSearchText = stringSearchTextField.getText();
		int fullSearchTextLength = fullSearchText.length();
		//get rid of any spaces, they're only good for loosening length verification
		char[] searchText = fullSearchText.replace(" ", "").toLowerCase().toCharArray();
		int searchTextLength = searchText.length;
		if (searchTextLength < 1)
			stringSearchResultsList.setEnabled(false);
		else {
			stringSearchResultsListModel.removeAllElements();
			searchableStringsFoundIndices.clear();
			//go through every string and see if it matches the input
			int searchableStringsLength = searchableStrings.length;
			char startChar = searchText[0];
			for (int i = 0; i < searchableStringsLength; i++) {
				char[] resultString = searchableStrings[i];
				int resultStringLength = resultString.length;
				//it can't start at an index if it would go past the end of the string
				int maxSearchIndex = resultStringLength - searchTextLength;
				//keep track of stats so that we can sort
				boolean stringPasses = false;
				int matchedLength = Integer.MAX_VALUE;
				//first, look for this char in the string
				for (int k = 0; k <= maxSearchIndex; k++) {
					//we found a spot where it starts
					//now go through and see if we found our string here
					//if we find one, keep looking through the string to see if we can find a shorter match
					if (resultString[k] == startChar) {
						//1 character strings always match single characters
						if (searchTextLength == 1) {
							stringPasses = true;
							break;
						} else {
							for (int j = 1, newK = k + 1; newK < resultStringLength;) {
								if (searchText[j] == resultString[newK]) {
									j++;
									//if we got through all our characters, the string matches
									if (j == searchTextLength) {
										stringPasses = true;
										matchedLength = Math.min(matchedLength, newK - k + 1);
										break;
									}
								}
								newK++;
								//the string appears not to match, stop searching here
								if (newK - k >= fullSearchTextLength * 2)
									break;
							}
						}
					}
				}
				if (stringPasses)
					searchableStringsFoundIndices.add(new mark(i, matchedLength, resultStringLength));
			}
			if (searchableStringsFoundIndices.size() <= MAX_SEARCH_RESULTS) {
				stringSearchResultsList.setEnabled(true);
				Collections.sort(searchableStringsFoundIndices);
				for (mark foundIndexStats : searchableStringsFoundIndices)
					stringSearchResultsListModel.addElement(allStringsListModel.get(foundIndexStats.searchableStringFoundIndex));
			} else {
				stringSearchResultsList.setEnabled(false);
				stringSearchResultsListModel.addElement("Please narrow your search to no more than " +
					MAX_SEARCH_RESULTS + " results (got " + searchableStringsFoundIndices.size() + ")");
			}
		}
	}
	public static void scrollToStringInList() {
		if (stringSearchResultsList.isEnabled())
			selectSearchableString(
				searchableStringsFoundIndices
					.get(stringSearchResultsList.getSelectedIndex())
					.searchableStringFoundIndex);
	}
	public static void selectSearchableString(int index) {
		allStringsList.setSelectedIndex(index);
		//make some room on both sides
		int spacing = (allStringsList.getVisibleRowCount() - 1) / 2;
		allStringsList.ensureIndexIsVisible(Math.max(0, index - spacing));
		allStringsList.ensureIndexIsVisible(Math.min(searchableStrings.length - 1, index + spacing));
	}
	public static void setStartingSaveAndString() {
		//default selection to the most recent string
		int newestStringIndex = foundStringsIndexList.size() - 1;
		if (newestStringIndex >= 0) {
			int foundIndex = foundStringsIndexList.get(newestStringIndex);
			lastSaveSelector.setSelectedIndex(
				findAbbreviation(
					extractAbbreviationFromString(allStringsListModel.get(foundIndex)) + ":"));
			selectSearchableString(foundIndex);
		}
	}
////////////////////////////////////////////////////////////////
////////	Modifying strings or save names
////////////////////////////////////////////////////////////////
	public static void markString(String customAbbreviation) {
		int selectedIndex = allStringsList.getSelectedIndex();
		String selectedString = allStringsList.getSelectedValue();
		String newString = null;
		if (selectedString.startsWith("#")) {
			//update the string
			newString = "    " + foundStringsIndexList.size() + "    " + currentSaveAbbreviation() + "    " + selectedString;
			foundStringsIndexList.add(selectedIndex);
		} else {
			saveAndTextTextField.setText("");
			if (produceSaveAndText("Select a new index", true, true)) {
				int oldIndex = Integer.parseInt(selectedString.substring(4, selectedString.indexOf(' ', 4)));
				//if we didn't input an index, just use the old index
				int newIndex = saveAndTextText.length() == 0 ? oldIndex : Integer.parseInt(saveAndTextText);
				int cappedIndex;
				int diff;
				//if we're removing it, put it past the end instead
				//this way, everything gets shifted backwards properly
				if (newIndex < 0)
					newIndex = foundStringsIndexList.size();
				if (newIndex > oldIndex) {
					cappedIndex = Math.min(foundStringsIndexList.size() - 1, newIndex);
					diff = 1;
				} else {
					cappedIndex = Math.max(0, newIndex);
					diff = -1;
				}
				//reorder and edit the other strings
				for (int toIndex = oldIndex; toIndex != cappedIndex;) {
					//this is where we will be getting the string+index from
					//we will then move it to oldIndex
					int fromIndex = toIndex + diff;
					//move the index
					int stringIndex = foundStringsIndexList.get(fromIndex);
					foundStringsIndexList.set(toIndex, stringIndex);
					//update the string
					String movedString = allStringsListModel.get(stringIndex)
						.replace(String.valueOf(fromIndex), String.valueOf(toIndex));
					allStringsListModel.set(stringIndex, movedString);
					searchableStrings[stringIndex] = movedString.toLowerCase().toCharArray();
					//and finally, update the index
					toIndex = fromIndex;
				}
				//if our index was in the bounds, insert it
				if (newIndex == cappedIndex) {
					foundStringsIndexList.set(newIndex, selectedIndex);
					//replace the index of the string
					newString = selectedString.replace(String.valueOf(oldIndex), String.valueOf(newIndex));
					//always replace the abbreviation in the string with the one in the save
					int abbreviationIndex = newString.indexOf(' ', 5) + 4;
					newString = newString.replace(
						newString.substring(abbreviationIndex, newString.indexOf(' ', abbreviationIndex)),
						currentSaveAbbreviation());
				//otherwise, strip it of its index
				//we made sure to shift everything backwards, so the last element is dead
				} else {
					foundStringsIndexList.remove(foundStringsIndexList.size() - 1);
					newString = selectedString.substring(selectedString.indexOf('#'));
				}
			}
		}
		//if we want to replace our string, replace it in the display list and the search list
		if (newString != null) {
			allStringsListModel.set(selectedIndex, newString);
			searchableStrings[selectedIndex] = newString.toLowerCase().toCharArray();
			needsSaving = true;
		}
	}
	public static void newSave() {
		saveAndTextTextField.setText(currentSaveName());
		//get the save name but make sure it has an unused abbreviation
		while (true) {
			//pick a save name
			if (!produceSaveAndText("Pick Parent Save + New Name", true, true) || saveAndTextText.length() == 0)
				break;
			//build the save abbreviation
			StringBuilder newSavePickerName = new StringBuilder(" ");
			int dashIndex = saveAndTextSave.indexOf('-');
			newSavePickerName.append(saveAndTextSave, 0, dashIndex + 1);
			buildAbbreviationTo(saveAndTextText, 0, newSavePickerName);
			//add the abbreviation num; 1 if it's new, something else if it's not
			String abbreviation = newSavePickerName.substring(dashIndex + 2);
			int newSaveNum = nextAvailableAbbreviationNum(abbreviation);
			if (newSaveNum > 1 && !confirm(
					"The abbreviation " + abbreviation + " is already in use. Create a new attempt?", "Already In Use"))
				continue;
			newSavePickerName.append(String.valueOf(newSaveNum));
			newSavePickerName.append(": ");
			newSavePickerName.append(saveAndTextText);
			//skip past any children of the parent save
			int newSaveIndex = skipChildren(saveAndTextParentSavePicker.getSelectedIndex() + 1, dashIndex);
			//insert it
			lastSaveSelectorModel.insertElementAt(newSavePickerName.toString(), newSaveIndex);
			lastSaveSelector.setSelectedIndex(newSaveIndex);
			mainWindow.pack();
			needsSaving = true;
			break;
		}
	}
	public static void newAttempt() {
		//find the abbreviation we'll use
		String lastSaveName = (String)(lastSaveSelectorModel.getSelectedItem());
		int colonIndex = lastSaveName.indexOf(':');
		if (confirm("Create a new attempt of " + lastSaveName.substring(colonIndex + 2) + "?", "Confirm New Attempt")) {
			int abbreviationIndex = lastSaveName.indexOf('-') + 1;
			String abbreviation = lettersAbbreviationAt(lastSaveName, abbreviationIndex);
			//insert it with the next number up
			int newSaveIndex = skipChildren(lastSaveSelector.getSelectedIndex() + 1, abbreviationIndex - 2);
			lastSaveSelectorModel.insertElementAt(
				lastSaveName.replace(
					lastSaveName.substring(abbreviationIndex + abbreviation.length(), colonIndex),
					String.valueOf(nextAvailableAbbreviationNum(abbreviation))),
				newSaveIndex);
			lastSaveSelector.setSelectedIndex(newSaveIndex);
			mainWindow.pack();
			needsSaving = true;
		}
	}
////////////////////////////////////////////////////////////////
////////	File load/save management
////////////////////////////////////////////////////////////////
	public static void loadBaseStrings() {
		//ensure the base strings file exists
		if (!extractedFileDir.exists())
			extractedFileDir.mkdir();
		if (!baseStringsFile.exists())
			extractDataWin();
		//find the latest numbered strings file
		int lastFileNum = 0;
		while ((new File(extractedFileDir, nextFileNum + ".txt")).exists()) {
			lastFileNum = nextFileNum;
			nextFileNum *= 2;
		}
		while (nextFileNum > lastFileNum + 1) {
			int mid = (nextFileNum + lastFileNum) / 2;
			if ((new File(extractedFileDir, mid + ".txt")).exists())
				lastFileNum = mid;
			else
				nextFileNum = mid;
		}
	}
	public static void loadStrings() {
		ArrayList<String> loadedStrings;
		//load the strings from the file we want to use
		if (latestStringsFile.exists())
			loadedStrings = loadStrings(latestStringsFile, true);
		else if (stringsPackedFile.exists())
			loadedStrings = loadStrings(stringsPackedFile, true);
		else
			loadedStrings = loadStrings(new File(extractedFileDir, (nextFileNum - 1) + ".txt"), true);
		for (String s : loadedStrings)
			allStringsListModel.addElement(s);
	}
	public static ArrayList<String> loadStrings(File f, boolean isPrimaryLoad) {
		//load the file and hierarchy
		ArrayList<String> fileStrings = loadFileStrings(f);
		int stringsListIndex = loadHierarchy(fileStrings, !isPrimaryLoad);
		//track which saves got used and which ones didn't
		boolean[] usedSaves = new boolean[lastSaveSelector.getItemCount()];
		//load the strings
		ArrayList<String> loadedStrings;
		if (f == stringsPackedFile)
			loadedStrings = loadFromPackedStringsFile(stringsListIndex, fileStrings, usedSaves);
		else
			loadedStrings = loadFromStringsFile(stringsListIndex, fileStrings, usedSaves);
		//verify the strings we got unless this is the base strings being loaded for the packed strings
		if (isPrimaryLoad) {
			logUnusedSaves(usedSaves);
			verifyStringsAreInOrder(loadedStrings);
		}
		return loadedStrings;
	}
	public static void saveMissingStringFiles() {
		//if we haven't found any strings to save, then there's nothing to save
		if (foundStringsIndexList.size() < 1)
			return;
		if (!latestStringsFile.exists())
			exportStrings();
		if (!stringsPackedFile.exists())
			exportStringsPacked();
		if (!stringTreeFile.exists())
			exportStringTree();
	}
////////////////////////////////////////////////////////////////
////////	Loading files
////////////////////////////////////////////////////////////////
	public static ArrayList<String> loadFileStrings(File f) {
		ArrayList<String> fileStrings = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null)
				fileStrings.add(line);
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return fileStrings;
	}
	//returns the index after the blank line after the hierarchy
	public static int loadHierarchy(ArrayList<String> fileStrings, boolean discardSaveHierarchy) {
		//build the saves hierarchy
		//skip the "Hierarchy:" line
		int saveStringsIndex = 1;
		int lastSaveIndent = 0;
		for (; true; saveStringsIndex++) {
			String saveName = fileStrings.get(saveStringsIndex);
			//we're done if we get an empty line, return the line after it
			if (saveName.length() == 0)
				return saveStringsIndex + 1;
			//when loading the packed strings, we load the base strings first and drop its hierarchy
			if (discardSaveHierarchy)
				continue;
			//add the string, but quit if hand-editing the strings file messed it up
			lastSaveIndent = verifySaveStringIsValid(saveName, lastSaveIndent);
			lastSaveSelector.addItem(saveName);
		}
	}
	public static ArrayList<String> loadFromStringsFile(int stringsListIndex, ArrayList<String> fileStrings, boolean[] usedSaves) {
		ArrayList<String> loadedStrings = new ArrayList<String>();
		//now fill the searchable strings list
		searchableStrings = new char[fileStrings.size() - stringsListIndex][];
		for (int i = 0; i < searchableStrings.length; i++) {
			//add the string to the list
			String s = fileStrings.get(i + stringsListIndex);
			//save its index if it has one
			if (s.charAt(0) != '#') {
				int secondTabIndex = s.indexOf('\t', 1);
				int foundStringsIndexListIndex = Integer.parseInt(s.substring(1, secondTabIndex));
				while (foundStringsIndexList.size() <= foundStringsIndexListIndex)
					foundStringsIndexList.add(null);
				foundStringsIndexList.set(foundStringsIndexListIndex, i);
				//also mark the save that it uses
				String abbreviation = s.substring(secondTabIndex + 1, s.indexOf('\t', secondTabIndex + 1));
				markAbbreviationUsed(abbreviation, usedSaves);
				//use spaces instead of tabs for the strings list
				s = s.replaceFirst("\t([^\t]*)\t([^\t]*)\t#", "    $1    $2    #");
			}
			loadedStrings.add(s);
			searchableStrings[i] = s.toLowerCase().toCharArray();
		}
		return loadedStrings;
	}
	public static ArrayList<String> loadFromPackedStringsFile(int stringsListIndex, ArrayList<String> fileStrings, boolean[] usedSaves) {
		//load the base strings before continuing
		ArrayList<String> loadedStrings = loadStrings(baseStringsFile, false);
		for (int i = stringsListIndex; i < fileStrings.size(); i++) {
			String[] indexAndAbbreviation = fileStrings.get(i).split("\t");
			String indexString = indexAndAbbreviation[0];
			String abbreviation = indexAndAbbreviation[1];
			int index = Integer.parseInt(indexString);
			foundStringsIndexList.add(index);
			markAbbreviationUsed(abbreviation, usedSaves);
			String newString = "    " + (i - stringsListIndex) + "    " + abbreviation + "    " + loadedStrings.get(index);
			loadedStrings.set(index, newString);
			searchableStrings[index] = newString.toLowerCase().toCharArray();
		}
		return loadedStrings;
	}
////////////////////////////////////////////////////////////////
////////	Verify loaded information
////////////////////////////////////////////////////////////////
	public static void markAbbreviationUsed(String abbreviation, boolean[] usedSaves) {
		int saveIndex = findAbbreviation(abbreviation + ":");
		if (saveIndex >= 0)
			usedSaves[saveIndex] = true;
		else {
			System.out.println("Could not find save name for " + abbreviation);
			System.exit(0);
		}
	}
	public static int verifySaveStringIsValid(String saveName, int lastSaveIndent) {
		//verify the save isn't too indented
		int dashIndex = saveName.indexOf('-');
		if (dashIndex > lastSaveIndent + 1) {
			System.out.println("Formatting error: need to de-indent " + saveName.substring(dashIndex));
			System.exit(0);
		}
		//verify the save doesn't have the same abbreviation as another save
		int colonIndex = saveName.indexOf(':');
		String abbreviation = saveName.substring(dashIndex + 1, colonIndex + 1);
		if (findAbbreviation(abbreviation) >= 0) {
			System.out.println("Copy error: duplicate use of " + abbreviation.substring(0, abbreviation.length() - 1));
			System.exit(0);
		}
		//check if its abbreviation matches its string
		String lettersAbbreviation = lettersAbbreviationAt(abbreviation, 0);
		if (!lettersAbbreviationAt(abbreviation, 0).equals(buildAbbreviation(saveName, colonIndex + 2))) {
			System.out.println("Save \"" + saveName.substring(colonIndex + 2) +
				"\" does not match abbreviation " + lettersAbbreviation);
		}
		//verify that it has a number
		if (lettersAbbreviation.length() == abbreviation.length() - 1) {
			System.out.println("Formatting error: abbreviation " + abbreviation + " is missing attempt number");
			System.exit(0);
		}
		return dashIndex;
	}
	public static void logUnusedSaves(boolean[] usedSaves) {
		for (int i = 0; i < usedSaves.length; i++) {
			if (!usedSaves[i]) {
				String s = lastSaveSelector.getItemAt(i);
				System.out.println("Unused save " + s.substring(s.indexOf('-')));
			}
		}
	}
	public static void verifyStringsAreInOrder(ArrayList<String> loadedStrings) {
		//now go through all the strings in export order and verify that the saves are in order
		HashMap<String, Integer> lastAbbreviationNums = new HashMap<String, Integer>();
		String lastAbbreviation = "";
		for (int i : foundStringsIndexList) {
			String abbreviation = extractAbbreviationFromString(loadedStrings.get(i));
			if (!abbreviation.equals(lastAbbreviation)) {
				String lettersAbbreviation = lettersAbbreviationAt(abbreviation, 0);
				int abbreviationNum = Integer.parseInt(abbreviation.substring(lettersAbbreviation.length()));
				Integer lastAbbreviationNum = lastAbbreviationNums.get(lettersAbbreviation);
				if (lastAbbreviationNum == null) {
					if (abbreviationNum != 1)
						System.out.println(abbreviation + " not preceded by any abbreviation");
				} else {
					if (abbreviationNum != lastAbbreviationNum + 1)
						System.out.println(abbreviation + " preceded by " + lettersAbbreviation + lastAbbreviationNum);
				}
				lastAbbreviationNums.put(lettersAbbreviation, abbreviationNum);
				lastAbbreviation = abbreviation;
			}
		}
	}
////////////////////////////////////////////////////////////////
////////	Saving files
////////////////////////////////////////////////////////////////
	public static void extractDataWin() {
		try {
			//find data.win
			System.out.println("Choosing data.win file...");
			LookAndFeel defaultLookAndFeel = UIManager.getLookAndFeel();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open your \"data.win\" file");
			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				throw new IOException("Could not retrieve data.win for extracting");
			UIManager.setLookAndFeel(defaultLookAndFeel);
			System.out.println("Extracting data.win");
			//read the file to a byte array
			File fIn = chooser.getSelectedFile();
			byte[] charsIn = new byte[(int)(fIn.length())];
			FileInputStream fis = new FileInputStream(fIn);
			int read = fis.read(charsIn);
			fis.close();
			//prepare the output
			byte[] charsOut = new byte[charsIn.length];
			int charsOutCount = 0;
			int length = charsIn.length;
			int linesWritten = 0;
			//these are the first and last strings in the region of strings we might care about
			byte[] startString = "Greetings.".getBytes();
			byte[] endString = "* (Looks like Mettaton is&  undergoing repairs.)/%%".getBytes();
			//search for the start string
			byte startChar = startString[0];
			int i = 0;
			while (charsIn[i] != startChar || !byteArrayAtIndexEquals(charsIn, i, startString))
				i++;
			//i is now the index of the first string
			//search through the list until we find our last string
			startChar = endString[0];
			while (true) {
				int startI = i;
				charsOut[charsOutCount] = '#';
				charsOut[charsOutCount + 1] = '#';
				charsOutCount += 2;
				//undertale strings are c-strings, find the \0
				while (charsIn[i] != 0) {
					charsOut[charsOutCount] = charsIn[i];
					charsOutCount++;
					i++;
				}
				charsOut[charsOutCount] = '\n';
				charsOutCount++;
				linesWritten++;
				if (charsIn[startI] == startChar && byteArrayAtIndexEquals(charsIn, startI, endString))
					break;
				//advance 5 bytes to get to the next string
				i += 5;
			}
			FileOutputStream fos = new FileOutputStream(baseStringsFile);
			fos.write(fileHeaderString.getBytes());
			fos.write("-NPS1: No Previous Save\n\n".getBytes());
			fos.write(charsOut, 0, charsOutCount);
			fos.close();
			System.out.println("Extracted data.win with " + linesWritten + " lines and " + charsOutCount + " bytes");
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	public static boolean byteArrayAtIndexEquals(byte[] source, int index, byte[] searchingFor) {
		int searchingForLength = searchingFor.length;
		//start at 1, assume that we've already compared the first character
		for (int i = 1; i < searchingForLength; i++) {
			if (source[index + i] != searchingFor[i])
				return false;
		}
		return true;
	}
	public static void exportStrings() {
		try {
			//if we have a strings.txt file, rename it to the next num
			//if we don't, we can write straight to it
			if (latestStringsFile.exists()) {
				String nextNumFileName = nextFileNum + ".txt";
				if (latestStringsFile.renameTo(new File(extractedFileDir, nextNumFileName)))
					nextFileNum++;
				else
					throw new IOException("Unable to rename " + latestStringsFile.getName() + " to " + nextNumFileName);
			}
			FileWriter writer = new FileWriter(latestStringsFile);
			FileWriter unusedWriter = new FileWriter(stringsUnusedFile);
			writeHeadersAndHierarchy(writer);
			for (int i = 0; i < allStringsListModel.getSize(); i++) {
				String line = allStringsListModel.get(i);
				if (line.charAt(0) == '#') {
					writer.write(line);
					unusedWriter.write(line);
					unusedWriter.write('\n');
				} else
					writer.write(line.replaceFirst("    ([^ ]*)    ([^ ]*)    #", "\t$1\t$2\t#"));
				writer.write('\n');
			}
			writer.close();
			unusedWriter.close();
			needsSaving = false;
			if (displaySaveSuccessMessages)
				JOptionPane.showMessageDialog(null, "Your file has been saved!");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void writeHeadersAndHierarchy(FileWriter writer) throws Exception {
		writer.write(fileHeaderString.toString());
		int itemCount = lastSaveSelector.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			writer.write(lastSaveSelector.getItemAt(i));
			writer.write('\n');
		}
		writer.write('\n');
	}
	public static void exportStringsPacked() {
		try {
			FileWriter writer = new FileWriter(stringsPackedFile);
			writeHeadersAndHierarchy(writer);
			for (int i = 0; i < foundStringsIndexList.size(); i++) {
				int foundStringIndex = foundStringsIndexList.get(i);
				writer.write(String.valueOf(foundStringIndex));
				writer.write('\t');
				writer.write(extractAbbreviationFromString(allStringsListModel.get(foundStringIndex)));
				writer.write('\n');
			}
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void exportStringTree() {
		try {
			FileWriter writer = new FileWriter(stringTreeFile);
			writeHeadersAndHierarchy(writer);
			writer.write("Found text strings:\n");
			String lastAbbreviation = "";
			ArrayList<String> lastParentSaves = new ArrayList<String>();
			int lastAbbreviationIndex = -1;
			String lastPrefix = null;
			for (int i = 0; i < foundStringsIndexList.size(); i++) {
				String foundString = allStringsListModel.get(foundStringsIndexList.get(i));
				String abbreviation = extractAbbreviationFromString(foundString);
				if (!abbreviation.equals(lastAbbreviation)) {
					//we want to find the parent list of saves up to and including the one from the abbreviation
					//start by finding the save name with the abbreviation
					ArrayList<String> parentSaves = new ArrayList<String>();
					int saveIndex = 0;
					int dashIndex = 0;
					for (; true; saveIndex++) {
						String saveName = lastSaveSelector.getItemAt(saveIndex);
						dashIndex = saveName.indexOf('-');
						//we found the save that goes with this abbreviation
						if (saveName.startsWith(abbreviation, dashIndex + 1)) {
							parentSaves.add(saveName);
							lastPrefix = saveName.substring(0, dashIndex) + "   -|";
							lastAbbreviation = abbreviation;
							break;
						}
					}
					//now that we have our index, go find all the saves above it
					while (dashIndex > 0) {
						dashIndex--;
						String saveName;
						for (saveIndex--;
							(saveName = lastSaveSelector.getItemAt(saveIndex))
								.indexOf('-') > dashIndex;
							saveIndex--)
							;
						parentSaves.add(saveName);
					}
					//now we have all our saves
					//skip past all common parents
					//but even if the new save is a parent of the last save, we want to keep it
					int parentSavesIndex = parentSaves.size() - 1;
					for (int lastParentSavesIndex = lastParentSaves.size() - 1;
							lastParentSavesIndex >= 0 &&
							parentSavesIndex >= 1 &&
							lastParentSaves
								.get(lastParentSavesIndex)
								.equals(parentSaves.get(parentSavesIndex));
							parentSavesIndex--)
						lastParentSavesIndex--;
					//add the rest to the output
					for (; parentSavesIndex >= 0; parentSavesIndex--) {
						writer.write(parentSaves.get(parentSavesIndex));
						writer.write('\n');
					}
					//and don't forget to save the last parent saves list
					lastParentSaves = parentSaves;
				}
				writer.write(lastPrefix);
				//skip the "##"
				writer.write(foundString.substring(foundString.indexOf('#') + 2));
				writer.write('\n');
			}
			writer.close();
			if (displaySaveSuccessMessages)
				JOptionPane.showMessageDialog(null, "Your tree has been exported!");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void backupSaves() {
		saveAndTextTextField.setText(currentSaveName());
		while (true) {
			if (!produceSaveAndText("Enter Backup Name", false, true))
				break;
			if (!backupFolder.exists())
				backupFolder.mkdir();
			File saveFolder = new File(backupFolder, saveAndTextText);
			if (saveFolder.exists()) {
				JOptionPane.showMessageDialog(null, "Backup folder " + saveAndTextText + " already exists");
				continue;
			}
			saveFolder.mkdir();
			for (int i = 0; i < backupFilesToCopy.length; i++) {
				File saveFile = new File(backupFilesToCopy[i]);
				if (!saveFile.exists()) {
					System.out.println("Could not find " + backupFilesToCopy[i]);
					continue;
				}
				try {
					byte[] fileBytes = new byte[(int)(saveFile.length())];
					FileInputStream fis = new FileInputStream(saveFile);
					fis.read(fileBytes);
					if (fis.read() != -1)
						throw new IOException("Unable to read entire file for " + saveFile.getName());
					fis.close();
					FileOutputStream fos = new FileOutputStream(new File(saveFolder, backupFilesToCopy[i]));
					fos.write(fileBytes, 0, fileBytes.length);
					fos.close();
				} catch(Exception ex) {
					ex.printStackTrace();
					System.out.println("Could not backup " + saveFile.getName());
				}
			}
			break;
		}
	}
////////////////////////////////////////////////////////////////
////////	Events
////////////////////////////////////////////////////////////////
	public void insertUpdate(DocumentEvent e) {
		refreshStringSearchResults();
	}
	public void changedUpdate(DocumentEvent e) {
		refreshStringSearchResults();
	}
	public void removeUpdate(DocumentEvent e) {
		refreshStringSearchResults();
	}
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == newSaveButton)
			newSave();
		else if (source == newAttemptButton)
			newAttempt();
		else if (source == backupButton)
			backupSaves();
		else if (source == exportStringsButton) {
			if (needsSaving ?
					confirm("Save your changes?", "Save Changes") :
					confirm("No changes detected. Save anyway?", "Save Not Needed"))
				exportStrings();
		} else if (source == exportStringTreeButton) {
			if (confirm("Export String Tree?", "Export Tree"))
				exportStringTree();
		} else
			System.out.println("Unknown action performed");
	}
	public void mousePressed(MouseEvent evt) {
		Object source = evt.getSource();
		if (source == stringSearchResultsList)
			scrollToStringInList();
		else if (source == allStringsList) {
			if (evt.getClickCount() == 2)
				markString(null);
		}
	}
	public void windowClosing(WindowEvent evt) {
		if (needsSaving) {
			int response = JOptionPane.showConfirmDialog(null, "You have unsaved changes.\nWould you like to save them before exiting?");
			if (response == JOptionPane.YES_OPTION)
				exportStrings();
			else if (response == JOptionPane.CANCEL_OPTION)
				return;
		}
		System.exit(0);
	}
	public void windowActivated(WindowEvent evt) {}
	public void windowClosed(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowOpened(WindowEvent evt) {}
	public void mouseClicked(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
////////////////////////////////////////////////////////////////
////////	Class for sorting string results
////////////////////////////////////////////////////////////////
	public int searchableStringFoundIndex;
	public int sortableMatchedLength;
	public int sortableTotalLength;
	public mark(int ssfi, int sml, int stl) {
		searchableStringFoundIndex = ssfi;
		sortableMatchedLength = sml;
		sortableTotalLength = stl;
	}
	public int compareTo(mark m) {
		if (sortableMatchedLength != m.sortableMatchedLength)
			return sortableMatchedLength - m.sortableMatchedLength;
		else
			return sortableTotalLength - m.sortableTotalLength;
	}
}