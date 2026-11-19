package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.FileCarRepository;
import com.revline.service.InventoryService;
import com.revline.service.UndoManager;
import com.revline.util.FileUtil;
import com.revline.util.exception.RevLineException;

import java.io.File;
import java.util.Collections;

/**
 * Tests for stack-based undo history (L6-7 Deque) and student exercise EX-7.
 */
public class UndoManagerTest {

    private static Car makeCar(int id, String brand, String model) {
        return new Car(id, brand, model, 250000.0, 320, FuelType.PETROL, 600, 3.2, Status.AVAILABLE);
    }

    public static void testPushAndPeek() {
        UndoManager undo = new UndoManager(10);
        Assert.assertFalse(undo.canUndo(), "Initial stack is empty");

        Car c1 = makeCar(1, "Ferrari", "Roma");
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, c1, "Added Ferrari Roma"));

        Assert.assertTrue(undo.canUndo(), "Stack has action");
        Assert.assertEquals(1, undo.size(), "Stack size is 1");
        Assert.assertEquals(UndoManager.ActionType.ADD, undo.peek().getType(), "Peek returns ADD action");
    }

    public static void testCapacityEviction() {
        UndoManager undo = new UndoManager(3);
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, makeCar(1, "B1", "M1"), "Action 1"));
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, makeCar(2, "B2", "M2"), "Action 2"));
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, makeCar(3, "B3", "M3"), "Action 3"));
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, makeCar(4, "B4", "M4"), "Action 4"));

        Assert.assertEquals(3, undo.size(), "Stack size capped at max capacity (3)");
        Assert.assertEquals("Action 4", undo.peek().getDescription(), "Top action is Action 4");
    }

    public static void testCanUndoAndClear() {
        UndoManager undo = new UndoManager(5);
        undo.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, makeCar(1, "B", "M"), "Desc"));
        undo.clear();

        Assert.assertFalse(undo.canUndo(), "Stack empty after clear");
        Assert.assertEquals(0, undo.size(), "Size is 0 after clear");
    }

    // =========================================================================
    // EXERCISE EX-7 CHECK METHOD
    // =========================================================================
    public static void testExercise7_undoOperation() throws RevLineException {
        File tempFile = new File("build/test_undo_cars.csv");
        if (tempFile.exists()) tempFile.delete();
        FileUtil.safeWrite(tempFile, Collections.singletonList(FileCarRepository.CSV_HEADER));

        CarRepository repo = new FileCarRepository(tempFile);
        UndoManager undoManager = new UndoManager(10);
        InventoryService service = new InventoryService(repo, undoManager, null);

        // 1. Test Undoing an ADD
        Car added = service.addCar(makeCar(0, "Aston Martin", "Vantage"));
        int addedId = added.getId();
        Assert.assertTrue(service.existsById(addedId), "Car was added");

        String undoAddResult = undoManager.undo(service);
        Assert.assertNotNull(undoAddResult, "Undo should return a confirmation message");
        Assert.assertFalse(service.existsById(addedId), "After undoing ADD, car should be removed from inventory");

        // 2. Test Undoing a DELETE
        Car reAdded = service.addCar(makeCar(0, "McLaren", "Artura"));
        int mclarenId = reAdded.getId();
        service.deleteCar(mclarenId);
        Assert.assertFalse(service.existsById(mclarenId), "Car was deleted");

        String undoDeleteResult = undoManager.undo(service);
        Assert.assertNotNull(undoDeleteResult, "Undo should return confirmation");
        Assert.assertTrue(service.existsById(mclarenId), "After undoing DELETE, car should be restored");

        // 3. Test Undoing an UPDATE
        Car updatedSpec = new Car(mclarenId, "McLaren", "Artura", 299000.0, 330, FuelType.HYBRID, 680, 3.0, Status.AVAILABLE);
        service.updateCar(mclarenId, updatedSpec);
        Assert.assertEquals(299000.0, service.findById(mclarenId).getPrice(), "Car price was updated");

        String undoUpdateResult = undoManager.undo(service);
        Assert.assertNotNull(undoUpdateResult, "Undo should return confirmation");
        Assert.assertEquals(250000.0, service.findById(mclarenId).getPrice(), "After undoing UPDATE, original price ($250k) is restored");

        tempFile.delete();
    }
}
