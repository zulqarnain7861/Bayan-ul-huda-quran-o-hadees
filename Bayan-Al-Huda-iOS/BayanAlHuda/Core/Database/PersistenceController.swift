import CoreData
import Foundation

@objc(BookmarkedItemEntity)
public class BookmarkedItemEntity: NSManagedObject {
    @NSManaged public var id: String
    @NSManaged public var category: String // "quran", "hadith", "qaida", "dua"
    @NSManaged public var referenceKey: String
    @NSManaged public var title: String
    @NSManaged public var arabicSnippet: String
    @NSManaged public var createdAt: Date
}

final class PersistenceController: ObservableObject {
    static let shared = PersistenceController()

    let container: NSPersistentContainer

    init(inMemory: Bool = false) {
        let model = Self.makeManagedObjectModel()
        container = NSPersistentContainer(name: "BayanAlHudaModel", managedObjectModel: model)

        if inMemory {
            container.persistentStoreDescriptions.first?.url = URL(fileURLWithPath: "/dev/null")
        }

        container.loadPersistentStores { _, error in
            if let error = error {
                NSLog("CoreData load error: \(error.localizedDescription)")
            }
        }
        container.viewContext.automaticallyMergesChangesFromParent = true
        container.viewContext.mergePolicy = NSMergeByPropertyObjectTrumpMergePolicy
    }

    func toggleBookmark(
        id: String,
        category: String,
        referenceKey: String,
        title: String,
        arabicSnippet: String
    ) {
        let context = container.viewContext
        let fetch = NSFetchRequest<BookmarkedItemEntity>(entityName: "BookmarkedItemEntity")
        fetch.predicate = NSPredicate(format: "id == %@", id)

        if let existing = try? context.fetch(fetch), let first = existing.first {
            context.delete(first)
        } else {
            let entity = BookmarkedItemEntity(context: context)
            entity.id = id
            entity.category = category
            entity.referenceKey = referenceKey
            entity.title = title
            entity.arabicSnippet = arabicSnippet
            entity.createdAt = Date()
        }

        if context.hasChanges {
            try? context.save()
        }
        objectWillChange.send()
    }

    func isBookmarked(id: String) -> Bool {
        let fetch = NSFetchRequest<BookmarkedItemEntity>(entityName: "BookmarkedItemEntity")
        fetch.predicate = NSPredicate(format: "id == %@", id)
        let count = (try? container.viewContext.count(for: fetch)) ?? 0
        return count > 0
    }

    private static func makeManagedObjectModel() -> NSManagedObjectModel {
        let model = NSManagedObjectModel()
        let entity = NSEntityDescription()
        entity.name = "BookmarkedItemEntity"
        entity.managedObjectClassName = NSStringFromClass(BookmarkedItemEntity.self)

        func attribute(_ name: String, _ type: NSAttributeType) -> NSAttributeDescription {
            let attr = NSAttributeDescription()
            attr.name = name
            attr.attributeType = type
            attr.isOptional = false
            return attr
        }

        entity.properties = [
            attribute("id", .stringAttributeType),
            attribute("category", .stringAttributeType),
            attribute("referenceKey", .stringAttributeType),
            attribute("title", .stringAttributeType),
            attribute("arabicSnippet", .stringAttributeType),
            attribute("createdAt", .dateAttributeType)
        ]
        model.entities = [entity]
        return model
    }
}
